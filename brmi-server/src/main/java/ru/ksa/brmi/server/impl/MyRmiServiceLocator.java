package ru.ksa.brmi.server.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import ru.ksa.brmi.api.MyRmiObjectResult;
import ru.ksa.brmi.api.MyRmiResult;
import ru.ksa.brmi.api.MyRmiVoidResult;
import ru.ksa.brmi.exception.MyRmiBaseException;
import ru.ksa.brmi.exception.MyRmiClassNotFoundException;
import ru.ksa.brmi.exception.MyRmiInvocationException;
import ru.ksa.brmi.exception.MyRmiNoSuchMethodException;
import ru.ksa.brmi.exception.MyRmiNoSuchParameterException;

public enum MyRmiServiceLocator {
	INSTANCE;

	private static ConcurrentHashMap<String, Object> registeredServices = new ConcurrentHashMap<>();

	public Object getService(String serviceName) {
		Object service = registeredServices.get(serviceName);
		return service;
	}

	public Object registerService(String serviceName, String className)
			throws MyRmiClassNotFoundException, MyRmiInvocationException {
		Objects.requireNonNull(serviceName, "Service name can't be null");
		Objects.requireNonNull(className, "Service class name can't be null");
		Object serviceInstance;
		try {
			serviceInstance = createServiceInstance(className);
		} catch (ClassNotFoundException e) {
			// Кидаем исключение когда запросили не существующий класс сервиса
			throw new MyRmiClassNotFoundException(
					String.format("Not found service key %s class %s", serviceName, className));
		} catch (InstantiationException e) {
			throw new MyRmiInvocationException(
					String.format("Instantiation service %s exception %s", serviceName, e.getMessage()));
		} catch (IllegalAccessException e) {
			throw new MyRmiInvocationException(
					String.format("Illegal access service %s exception %s", serviceName, e.getMessage()));
		}
		return registeredServices.put(serviceName, serviceInstance);
	}

	public void removeService(String serviceName) {
		registeredServices.remove(serviceName);
	}

	public static Object createServiceInstance(String className)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class<?> clazz = MyRmiServiceLocator.class.getClassLoader().loadClass(className);
		return clazz.newInstance();
	}

	public static MyRmiResult<?> invokeService(String serviceName, String methodName, Object[] params)
			throws MyRmiBaseException {
		return invokeService(INSTANCE.getService(serviceName), methodName, params);
	}

	public static MyRmiResult<?> invokeService(Object service, String methodName, Object[] params)
			throws MyRmiNoSuchMethodException, MyRmiNoSuchParameterException, MyRmiInvocationException {
		@SuppressWarnings("rawtypes")
		Class[] parameterClasses = new Class[params.length];
		for (int i = 0; i < params.length; i++) {
			parameterClasses[i] = params[i].getClass();
		}
		try {
			/*
			 * Реализуем требование: - Специально обрабатывать на сервере
			 * ситуацию, когда запросили несуществующий сервис или метод, или
			 * указали параметры неправильных типов, или неправильное количество
			 * параметров, и передавать это клиенту.
			 */
			Method method = null;
			boolean isInvalidParamCount = true;
			for (Method m : service.getClass().getMethods()) {
				if (m.getName().equals(methodName)) {
					method = m;
					if (params.length == method.getParameterCount()) {
						// Если есть метод с таким количеством параметров то
						// считаем, что все ОК и выходим из проверки
						isInvalidParamCount = false;
						break;
					}
				}
			}
			if (method == null) {
				// Кидаем исключение когда запросили не существующий метод
				throw new MyRmiNoSuchMethodException(String.format("Not found the method %s in service class %s",
						methodName, service.getClass().getName()));
			}
			if (isInvalidParamCount) {
				// Кидаем исключение когда запросили не правильное количество
				// параметров
				throw new MyRmiNoSuchParameterException(String.format("Illegal count argument % for service class %s.",
						Arrays.toString(params), service.getClass().getName()));
			}
			// Верхний блок в топку если реализация без дополнительных заморочек
			// на проверку количества
			// параметров!
			method = service.getClass().getMethod(methodName, parameterClasses);
			method.setAccessible(true);
			method.getReturnType();
			Object obj = method.invoke(service, params);
			// Специальным образом передавать возвращаемое значение в случае
			// когда функция void
			// Возвращает MyRmiVoidResult если функция Void.TYPE
			// Возвращает MyRmiObjectResult если функция возвращает результат
			if (method.getReturnType().equals(Void.TYPE)) {
				return new MyRmiVoidResult();
			}
			return new MyRmiObjectResult<>(obj);
		} catch (NoSuchMethodException e) {
			// Если не нашли метод по полной сигнатуре то считаем, что "Кидаем
			// исключение когда запросили не правильные типы
			// параметров"
			throw new MyRmiNoSuchParameterException(String.format("Illegal type parameters %s for service class %s.",
					Arrays.toString(parameterClasses), service.getClass().getName()));
		} catch (IllegalArgumentException e) {
			throw new MyRmiNoSuchParameterException(String.format("Illegal argument %s for service class %s.",
					Arrays.toString(params), service.getClass().getName()));
		} catch (IllegalAccessException e) {
			throw new MyRmiInvocationException(String.format("Illegal access service %s exception %s",
					service.getClass().getName(), e.getMessage()));
		} catch (InvocationTargetException e) {
			throw new MyRmiInvocationException(
					String.format("Invocation service %s exception %s", service.getClass().getName(), e.getMessage()));
		} catch (SecurityException e) {
			throw new MyRmiInvocationException(
					String.format("Security service %s exception %s", service.getClass().getName(), e.getMessage()));
		}
	}
}

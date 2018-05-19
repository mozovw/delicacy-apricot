package com.delicacy.apricot.command.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import com.delicacy.apricot.command.util.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component//声明这个类是被SpringIOC容器来管理的，如果不声明，就无法做到自动织入
@Aspect//这个类被声明为是一个需要动态织入到我们的虚拟切面中的类
public class AnnotationAspect {

	//声明切点
	//因为要利用反射机制去读取这个切面中的所有的注解信息
	@Pointcut("execution(* com.delicacy.apricot.command.runner..*(..))")
	public void pointcutConfig(){}


	@Around("pointcutConfig()")//
	public Object around(ProceedingJoinPoint point) throws Throwable {
		//go to
		Method method = null;
		try {
			method = point.getTarget().getClass().getDeclaredMethod("run", String[].class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		if (method==null)return null;
		//要不要继续往下
		Object[] args = point.getArgs();
		if (args.length==0)return null;
		String[] args1 = (String[]) args[0];
		if (args1.length==0)return null;
		String arg =  args1[0];
		String substring = arg.substring(1)+"r";
		String simpleName = point.getTarget().getClass().getSimpleName();
		String upperCase = getUpperCase(simpleName);
		if (!upperCase.toLowerCase().equals(substring))return null;
		//continue
		String methodName = method.getName();
		String name = method.getDeclaringClass().getName();
		log.info("--------------- start {} ---------------" ,name+"."+methodName+"()");
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();
		Object result = point.proceed();
		stopwatch.stop();
		log.info("--------------- end {}, using {} ms---------------" ,name+"."+methodName+"()",stopwatch.getElapsedTime());
		return result;
	}

	private String getUpperCase(String string){
		char[] chars = string.toCharArray();
		StringBuilder s = new StringBuilder();
		for (Character c :
				chars) {
			if (Character.isUpperCase(c)){
				s.append(c);
			}
		}
		return s.toString();
	}

	/*@Before("pointcutConfig()")
	public void before(JoinPoint joinPoint){
		try {
			Method method = joinPoint.getTarget().getClass().getDeclaredMethod("run", String[].class);
			String methodName = method.getName();
			String name = method.getDeclaringClass().getName();
			log.info("--------------- start {} ---------------" ,name+"."+methodName+"()");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	@After("pointcutConfig()")
	public void after(JoinPoint joinPoint){
		try {
			Method method = joinPoint.getTarget().getClass().getDeclaredMethod("run", String[].class);
			String methodName = method.getName();
			String name = method.getDeclaringClass().getName();
			log.info("--------------- end {} ---------------" ,name+"."+methodName+"()");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}*/
	
	/*@AfterReturning(returning="returnValue",value="pointcutConfig()")
	public void afterReturn(JoinPoint joinPoint,Object returnValue){
		log.info("调用获得返回值" + returnValue);
	}

	@AfterThrowing("pointcutConfig()")
	public void afterThrow(JoinPoint joinPoint){
		System.out.println("切点的参数" + Arrays.toString(joinPoint.getArgs()));
		System.out.println("切点的方法" + joinPoint.getKind());
		System.out.println(joinPoint.getSignature());
		System.out.println(joinPoint.getTarget()); //生成以后的代理对象
		System.out.println(joinPoint.getThis());//当前类的本身(通过反射机制去掉用)
		log.info("抛出异常之后执行" + joinPoint);
	}*/
}
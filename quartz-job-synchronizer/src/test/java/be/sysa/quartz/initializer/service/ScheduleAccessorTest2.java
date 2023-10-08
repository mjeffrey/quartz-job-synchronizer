package be.sysa.quartz.initializer.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.quartz.JobDetail;
import org.quartz.Scheduler;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Slf4j
public class ScheduleAccessorTest2 {



    ScheduleAccessor scheduleAccessor;
    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        //scheduler = SchedulerImpl.createProxy(Scheduler.class);
        scheduler = Mockito.mock(Scheduler.class);
        scheduleAccessor = new ScheduleAccessor(scheduler);
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("methods")
    public void callMethod(Method method) {
        Method accessorMethod = ScheduleAccessor.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
        Object[] params = getSchedulerAccessorParams(method);

        log.info( "method {} params {}", accessorMethod.getName(), accessorMethod.getParameterCount());
        Object invoke = accessorMethod.invoke(scheduleAccessor, params);
        assertMethodInvoked(scheduler, method, getSchedulerParams(method), times(1));
    }

    private Object[] getSchedulerParams(Method method) {
        log.info("Method {}", method.getName());
        Parameter[] parameters = method.getParameters();
        Object[] values =  new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramClass = parameters[i].getType();
            if( Objects.equals(paramClass, String.class)){
                values [i] = "DUMMY_NAME";
            }else{
                values[i] = Mockito.mock(paramClass);
            }
        }
        return values;
    }

    private Object[] getSchedulerAccessorParams(Method method) {
        log.info("Method {}", method.getName());
        if ( method.getName().equals("addJob") ){
            return new Object[]{Mockito.mock(JobDetail.class),  true, true};
        }
        Parameter[] parameters = method.getParameters();
        Object[] values =  new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramClass = parameters[i].getType();
            if( Objects.equals(paramClass, String.class)){
                values [i] = "DUMMY_NAME";
            }else{
                values[i] = Mockito.mock(paramClass);
            }
        }
        return values;
    }

    private <R> R typeOf(Parameter parameter) {
        return null;
    }

    private void assertMethodInvoked(Object object,
                                     Method method,
                                     Object[] params,
                                     VerificationMode verificationMode) throws Exception {
        final Method mockMethod = object.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        final Object verify = verify(object, verificationMode);
        mockMethod.invoke(verify, params );
    }


    static Stream<Method> methods() {
        Set<String> excludes = Set.of("toString", "hashCode", "equals", "getScheduler");
        List<Method> methods = asList(ScheduleAccessor.class.getDeclaredMethods());
        methods.forEach(m->m.setAccessible(true));
        return methods.stream()
                .filter(m-> Objects.equals(m.getDeclaringClass(), ScheduleAccessor.class))
                .filter( m-> !excludes.contains(m.getName()) )
                .sorted(Comparator.comparing(Method::getName))
                ;
    }

}

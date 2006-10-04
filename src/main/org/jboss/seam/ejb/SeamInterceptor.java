/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.ejb;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.Component;
import org.jboss.seam.InterceptorType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.interceptors.EventType;
import org.jboss.seam.interceptors.SeamInvocationContext;

/**
 * Interceptor for bijection and conversation scope management
 * for a session bean component
 * 
 * @author Gavin King
 */
public class SeamInterceptor implements Serializable
{
   
   public static ThreadLocal<Component> COMPONENT = new ThreadLocal<Component>();
   
   private static final Log log = LogFactory.getLog(SeamInterceptor.class);
   
   private final InterceptorType type;
   private boolean isSeamComponent;
   private String componentName;
   private transient Component component;
   private List<Object> userInterceptors;
   
   /**
    * Called when instatiated by EJB container.
    * (In this case it might be a Seam component,
    * but we won't know until postConstruct() is
    * called.)
    */
   public SeamInterceptor()
   {
      type = InterceptorType.SERVER;
   }
   
   /**
    * Called when instantiated by Seam.
    * (In this case it is always a Seam
    * component.)
    */
   public SeamInterceptor(InterceptorType type, Component component)
   {
      this.type = type;
      init(component);
   }

   private void init(Component component)
   {
      isSeamComponent = true;
      this.component = component;
      userInterceptors = component.createUserInterceptors(type);
      componentName = component.getName();
   }
   
   @PostConstruct
   public void postConstruct(InvocationContext invocation)
   {
      Object bean = invocation.getTarget();

      // if it is a session bean instantiated by the EJB 
      // container, we still need to init the component 
      // reference
      if (component==null) //ie. if it was instantiated by the EJB container
      {
         initSessionBean(bean);
      }
      
      // initialize the bean instance
      if (isSeamComponent)
      {
         try
         {
            component.initialize(bean);
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new RuntimeException("exception initializing EJB component", e);
         }
      }
      
      invokeAndHandle(invocation, EventType.POST_CONSTRUCT);
   }

   private void initSessionBean(Object bean)
   {
      Component invokingComponent = COMPONENT.get();
      if ( invokingComponent!=null )
      {
         //the session bean was obtained by the application by
         //calling Component.getInstance(), could be a role
         //other than the default role
         init(invokingComponent);
      }
      else if ( bean.getClass().isAnnotationPresent(Name.class) )
      {
         //the session bean was obtained by the application from
         //JNDI, so assume the default role
         String defaultComponentName = bean.getClass().getAnnotation(Name.class).value();
         init( Seam.componentForName( defaultComponentName ) );
      }
      else
      {
         isSeamComponent = false;
      }
   }

   @PreDestroy
   public void preDestroy(InvocationContext invocation)
   {
      invokeAndHandle(invocation, EventType.PRE_DESTORY);
   }
   
   @PrePassivate
   public void prePassivate(InvocationContext invocation)
   {
      invokeAndHandle(invocation, EventType.PRE_PASSIVATE);
   }
   
   @PostActivate
   public void postActivate(InvocationContext invocation)
   {
      if (isSeamComponent) component = Seam.componentForName(componentName);
      invokeAndHandle(invocation, EventType.POST_ACTIVATE);
   }
   
   private void invokeAndHandle(InvocationContext invocation, EventType invocationType)
   {
      try
      {
         invoke(invocation, invocationType);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("exception in EJB lifecycle callback", e);
      }
   }
   
   @AroundInvoke
   public Object aroundInvoke(InvocationContext invocation) throws Exception
   {
      return invoke(invocation, EventType.AROUND_INVOKE);
   }
   
   private Object invoke(InvocationContext invocation, EventType invocationType) throws Exception
   {
      if ( !isSeamComponent )
      {
         //not a Seam component
         return invocation.proceed();
      }
      else if ( Contexts.isEventContextActive() || Contexts.isApplicationContextActive() ) //not sure about the second bit (only needed at init time!)
      {
         //a Seam component, and Seam contexts exist
         return invokeInContexts(invocation, invocationType);
      }
      else
      {
         //if invoked outside of a set of Seam contexts,
         //set up temporary Seam EVENT and APPLICATION
         //contexts just for this call
         Lifecycle.beginCall();
         try
         {
            return invokeInContexts(invocation, invocationType);
         }
         finally
         {
            Lifecycle.endCall();
         }
      }
   }

   private Object invokeInContexts(InvocationContext invocation, EventType eventType) throws Exception
   {
      if ( isProcessInterceptors(component) )
      {
         if ( log.isTraceEnabled() ) 
         {
            log.trace("intercepted: " + component.getName() + '.' + invocation.getMethod().getName());
         }
         return new SeamInvocationContext( invocation, eventType, userInterceptors, component.getInterceptors(type) ).proceed();
      }
      else {
         if ( log.isTraceEnabled() ) 
         {
            log.trace("not intercepted: " + component.getName() + '.' + invocation.getMethod().getName());
         }
         return invocation.proceed();
      }
   }

   private boolean isProcessInterceptors(final Component component)
   {
      return component!=null && component.getInterceptionType().isActive();
   }
   
   public Component getComponent()
   {
      return component;
   }
   
}

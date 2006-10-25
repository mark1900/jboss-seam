//$Id$
package org.jboss.seam.example.registration.test;

import org.jboss.seam.Component;
import org.jboss.seam.example.registration.Register;
import org.jboss.seam.example.registration.User;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class RegisterTest extends SeamTest
{
   
   @Test
   public void testLogin() throws Exception
   {
            
      new Script() {

         @Override
         protected void updateModelValues() throws Exception
         {
            User user = (User) Component.getInstance("user", true);
            assert user!=null;
            user.setUsername("1ovthafew");
            user.setPassword("secret");
            user.setName("Gavin King");
         }

         @Override
         protected void invokeApplication()
         {
            Register register = (Register) Component.getInstance("register", true);
            String outcome = register.register();
            assert "/registered.jspx".equals( outcome );
         }

         @Override
         protected void renderResponse()
         {
            User user = (User) Component.getInstance("user", false);
            assert user!=null;
            assert user.getName().equals("Gavin King");
            assert user.getUsername().equals("1ovthafew");
            assert user.getPassword().equals("secret");
         }
         
      }.run();
      
   }
   
}

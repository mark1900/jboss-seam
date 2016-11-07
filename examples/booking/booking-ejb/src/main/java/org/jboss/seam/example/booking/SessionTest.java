package org.jboss.seam.example.booking;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name( "sessionTest" )
@Scope( ScopeType.SESSION )
public class SessionTest
{
    long num = 0;
    public String getTest()
    {
        num++;
        System.out.println("num: " + num);
        return String.valueOf(num);
    }

}

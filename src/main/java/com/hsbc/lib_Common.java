package com.hsbc;

import com.virtusa.isq.vtaf.runtime.SeleniumTestBase;
import org.openqa.selenium.By;

/**
 *  Class lib_Common contains reusable business components 
 *  Each method in this class correspond to a reusable business component.
 */
public class lib_Common {

    /**
     *  Business component bc_login.
     */
    public final static void bc_login(final SeleniumTestBase caller, final String username, final String password) throws Exception {
        caller.open("http://localhost/banking/","5000");
        caller.type("login.tf_UserName",username);
        caller.type("login.tf_Password",password);
        caller.click("login.btn_SignIn");	
    }
    
}

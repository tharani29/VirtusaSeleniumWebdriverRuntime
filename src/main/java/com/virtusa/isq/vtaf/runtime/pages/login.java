package com.virtusa.isq.vtaf.runtime.pages;

/**
 *  Class Login implements corresponding UI page
 *  UI objects in the page are stored in the class.
 */

public enum login {

        tf_UserName("//input[@id='txtUserId']"), tf_Password("//input[@name='txtPassword']"), btn_SignIn("//input[@value='Sign In']");

    private String searchPath;
  
    /**
    *  Page login.
    */
    private login(final String psearchPath) {
        this.searchPath = psearchPath;
    }
    
    /**
     *  Get search path.
     * @param searchPath search path.
     */
    public final String getSearchPath() {
        return searchPath;
    }
}
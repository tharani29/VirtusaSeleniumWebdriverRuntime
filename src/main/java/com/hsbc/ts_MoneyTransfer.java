package com.hsbc;

import java.util.HashMap;
import java.util.List;

import com.virtusa.isq.vtaf.aspects.VTAFRecoveryMethods;
import com.virtusa.isq.vtaf.runtime.SeleniumTestBase;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import com.virtusa.isq.vtaf.runtime.VTAFTestListener;


/**
 *  Class ts_MoneyTransfer implements corresponding test suite
 *  Each test case is a test method in this class.
 */

@Listeners (VTAFTestListener.class)
public class ts_MoneyTransfer extends SeleniumTestBase {



    /**
     * Data provider for Test case tc_001.
     * @return data table
     */
    @DataProvider(name = "tc_001")
    public Object[][] dataTable_tc_001() {     	
    	return this.getDataTable("login","accBalanceType","errorMessages");
    }

    /**
     * Data driven test case tc_001.
     *
     * @throws Exception the exception
     */
    @VTAFRecoveryMethods(onerrorMethods = {}, recoveryMethods = {}) 
    @Test (dataProvider = "tc_001")
    public final void tc_001(final String login_Username, final String login_Password, final String accBalanceType_Checking_Account, final String accBalanceType_Money_Market, final String accBalanceType_SavingsAccount, final String accBalanceType_Asset_Management, final String accBalanceType_BalanceType, final String accBalanceType_CheckAccount, final String accBalanceType_MonMarket, final String accBalanceType_SaveAccount, final String accBalanceType_AssetManagementAccount, final String accBalanceType_GBP, final String errorMessages_SameTransfer, final String errorMessages_TransactionLimit, final String errorMessages_Comment_S) throws Exception {	
    	lib_Common.bc_login(this, login_Username,login_Password);
    } 
    	

    public final Object[][] getDataTable(final String... tableNames) {
        String[] tables = tableNames;
        return this.getTableArray(getVirtualDataTable(tables));
    }

}
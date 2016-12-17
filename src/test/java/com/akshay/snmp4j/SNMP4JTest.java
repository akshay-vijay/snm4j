package com.akshay.snmp4j;

import org.junit.Before;
import org.junit.Test;

import com.akshay.snmp4j.TrapSender;


public class SNMP4JTest {
	
	TrapSender ts = null;
	boolean isConnectedToSNMP;
	public SNMP4JTest() {
		ts = new TrapSender();
	}
	
	@Before
	public void TestcheckSNMPServer() {
		
		if(ts.checkSNMPServer()) {
			assert(true);
			isConnectedToSNMP = true;
		}
		else
			assert(false);
	}
	
	@Test
	public void TestsendSnmpV1Trap() {
		
		if(ts.sendSnmpV1Trap() && isConnectedToSNMP)
			assert(true);
		else
			assert(false);
	}
	
	@Test
	public void TestsendSnmpV2Trap() {
		
		if(ts.sendSnmpV2Trap() && isConnectedToSNMP)
			assert(true);
		else
			assert(false);
	}
	
}



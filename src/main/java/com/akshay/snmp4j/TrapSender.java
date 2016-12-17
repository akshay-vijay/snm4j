package com.akshay.snmp4j;


import java.io.IOException;
import java.util.Date;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


public class TrapSender {
  
  public static final String  community  = "public";

  //  Sending Trap for sysLocation of RFC1213
  public static final String  trapOid          = ".1.3.6.1.2.1.2.2.1";                         

  public static final String  ipAddress      = "127.0.0.1";
  
  public static final int     port      = 162;
  
  public TrapSender() {
	  
  }

  /**
   * Main method to test and send traps to SNMP server
   */
  public static void main(String[] args) {
    TrapSender snmp4JTrap = new TrapSender();

    /* Check SNMP server is connected or not*/
    snmp4JTrap.checkSNMPServer();
    
    /* Sending V1 Trap */
    snmp4JTrap.sendSnmpV1Trap();

    /* Sending V2 Trap */
    snmp4JTrap.sendSnmpV2Trap();
  }

  /**
   * This methods sends the V1 trap to the Localhost in port 163
   */
  public boolean sendSnmpV1Trap() {
    try
    {
      TransportMapping transport = new DefaultUdpTransportMapping(); //Create Transport Mapping
      transport.listen();

      CommunityTarget comtarget = new CommunityTarget();
      comtarget.setCommunity(new OctetString(community));
      comtarget.setVersion(SnmpConstants.version2c);
      comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
      comtarget.setRetries(1);
      comtarget.setTimeout(5000);
      
      OID myOID = new OID(".1.3.6.1.2.1.2.2.1");

      PDUv1 pdu = new PDUv1(); //Create PDU for V1
      pdu.setType(PDU.V1TRAP);
      pdu.setEnterprise(new OID(trapOid));
      pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
      pdu.setSpecificTrap(1);
      pdu.setAgentAddress(new IpAddress(ipAddress));
      
      Long tstamp = System.currentTimeMillis();
      int i = tstamp.intValue();
      pdu.setTimestamp(new Long(i));
      pdu.add(new VariableBinding(myOID, new Integer32(i))); // Example how to add Varibale Bidning 

      Snmp snmp = new Snmp(transport); //Send the PDU
      
      System.out.println("Sending V1 Trap to " + ipAddress + " on Port " + port);
      snmp.send(pdu, comtarget);
      snmp.close();
      
      System.out.println("Sent V1 Trap to " + ipAddress + " on Port " + port);
      return true;
    }
    catch (Exception e)
    {
      System.err.println("Error in Sending V1 Trap to " + ipAddress + " on Port " + port);
      System.err.println("Exception Message = " + e.getMessage());
      e.printStackTrace();
    }
    
    return false;
  }

  
  /**
   * This methods sends the V2 trap to the Localhost in port 163
   */
  public boolean sendSnmpV2Trap()
  {
    try
    {
      TransportMapping transport = new DefaultUdpTransportMapping();
      transport.listen();

      CommunityTarget comtarget = new CommunityTarget();
      comtarget.setCommunity(new OctetString(community));
      comtarget.setVersion(SnmpConstants.version2c);
      comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
      comtarget.setRetries(1);
      comtarget.setTimeout(1000);

      PDU pdu = new PDU(); //Create PDU for V2
      
      // need to specify the system up time
      pdu.add(new VariableBinding(SnmpConstants.sysUpTime, new OctetString(new Date().toString())));
      pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(trapOid)));
      pdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress, new IpAddress(ipAddress)));
      pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.11.14.7.2.4.23.1.3"), new Integer32(1)));

      pdu.add(new VariableBinding(new OID(trapOid), new OctetString("Major"))); 
      pdu.setType(PDU.TRAP);
      pdu.isConfirmedPdu();
      pdu.isResponsePdu();
      
      Snmp snmp = new Snmp(transport);
      
      System.out.println("Sending V2 Trap to " + ipAddress + " on Port " + port);
      ResponseEvent response = snmp.send(pdu, comtarget);
      snmp.close();
      
      System.out.println("Sent V2 Trap to " + ipAddress + " on Port " + port);
      return true;
    }
    catch (Exception e)
    {
      System.err.println("Error in Sending V2 Trap to " + ipAddress + " on Port " + port);
      System.err.println("Exception Message = " + e.getMessage());
      e.printStackTrace();
    }
    
    return false;
  }
  
  public boolean checkSNMPServer() {
		
		Snmp snmp = null;

	    try
	    {
	      TransportMapping transport = new DefaultUdpTransportMapping();
	      transport.listen();

	      CommunityTarget comtarget = new CommunityTarget();
	      comtarget.setCommunity(new OctetString("public"));
	      comtarget.setVersion(SnmpConstants.version2c);
	      comtarget.setAddress(new UdpAddress(ipAddress + "/" + port));
	      comtarget.setRetries(1);
	      comtarget.setTimeout(1000);

	      PDUv1 pdu = new PDUv1();
	      
	      pdu.setType(PDU.INFORM); // PDU.INFORM will send us Response from SNMP server

	      snmp = new Snmp(transport);
	      ResponseEvent response = snmp.send(pdu, comtarget);
	      boolean success = true;
	      if (response != null) {
	    	  PDU responsePDU = response.getResponse();
	    	  if (responsePDU != null) {
	    		  if (responsePDU.getErrorStatus() != PDU.noError) {
	    			  success = false;
	    		  }
	    	  }
	    	  else {
	    		  success = false;
	    	  }
	      }
	      else {
	    	  success = false;
	      }

	      if(success) {
	    	  System.out.println("Connected to SNMP server at " + ipAddress + " on Port " + port);
	    	  return true;
	      } 
	      else {
	    	  System.out.println("Unable to connect to SNMP server at " + ipAddress + " on Port " + port);
	      }
	    }
	    catch (Exception e)
	    {
	    	System.out.println("Error in connecting to SNMP server at " + ipAddress + " on Port " + port);
	    }
	    finally {
	    	try {
	    		if(snmp != null) snmp.close();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    return false;
	}
}
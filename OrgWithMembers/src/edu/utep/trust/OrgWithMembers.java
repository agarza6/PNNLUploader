/**
Copyright (c) 2012, University of Texas at El Paso
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/


package edu.utep.trust;

import java.net.*;
import java.io.*;

import com.hp.hpl.jena.query.*;


public class OrgWithMembers{

	
	public static String getOrgMembersContent(String URL) {

		String URI = "";
		String inputLine, finalFile = "";

		try{
			URL oracle = new URL(URL);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							oracle.openStream()));

			while ((inputLine = in.readLine()) != null){

				if(inputLine.contains("<pmlp:Organization rdf:about=")){
					//Example of line: <pmlp:Organization rdf:about='http://rio.cs.utep.edu/ciserver/ciprojects/pmlp/JPL.owl#JPL'>
					URI = inputLine.substring(inputLine.indexOf('=') + 2, inputLine.lastIndexOf('>') - 1);
				}

				if(inputLine.contains("</pmlp:Organization>")){
					break;
				}
				finalFile = finalFile + '\n' + inputLine;
			}


			String tempM = queryMembers(URI);
			String[] members = tempM.split(",");
			for(int i = 0; i < members.length; i++){
				if(!members[i].isEmpty() || members[i].length() > 0)
					finalFile = finalFile + '\n' + "<pmlp:hasMember rdf:resource='" + members[i] + "'/>";
			}


			//finalize File
			finalFile = finalFile + '\n' + "</pmlp:Organization>";
			finalFile = finalFile + '\n' + "</rdf:RDF>";

			in.close();

			System.out.println(finalFile);
			
			return URLEncoder.encode(finalFile, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
		
	}  

	
	public static String queryMembers(String URI) {

		edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
		edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();

		String query = "PREFIX pmlp: <http://inference-web.org/2.0/pml-provenance.owl#> SELECT ?member WHERE {?member pmlp:isMemberOf <" + URI + ">}";

		String members = proxy.doQuery(query);

		ResultSet results = ResultSetFactory.fromXML(members);

		//		System.out.println(members);
		//		System.out.println(results);

		String memberList = "";

		if(results != null)
			while(results.hasNext()){

				String member = results.nextSolution().get("?member").toString();

				if(member != null){
					System.out.println(" -> " + member);
					memberList = memberList +  "," + member;
				}
			}
		return memberList;
	}

	public static void main(String[] args){

		System.out.println(getOrgMembersContent("http://rio.cs.utep.edu/ciserver/ciprojects/pmlp/NASA.owl"));
	}

}

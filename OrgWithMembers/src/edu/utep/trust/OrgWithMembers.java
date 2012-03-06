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

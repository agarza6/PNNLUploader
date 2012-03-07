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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;


/**
 * Servlet implementation class OrgWithMembersServlet
 */
public class OrgWithMembersServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	String contextPath = null;
	String URI = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OrgWithMembersServlet() {
		super();
	}

	public void init(ServletConfig config)throws ServletException {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String _URL;

		contextPath = request.getContextPath();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		//	    ServletContext context = getServletContext();

		//====================================================
		// URL/URI
		//====================================================
		_URL = request.getParameter("URL");

		if(_URL == null){
			out.print("URL IS NEEDED");
		}else{
			String PML_P = getOrgMembersContent(_URL);
			
			out.println(PML_P);
		}
	}

	public String getOrgMembersContent(String URL) {

		
		String inputLine, finalFile = "";

		try{
			System.out.println(URL);
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
				finalFile = finalFile + '\n' + "<pmlp:hasMember rdf:resource='" + members[i] + "'/>";
			}


			//finalize File
			finalFile = finalFile + '\n' + "</pmlp:Organization>";
			finalFile = finalFile + '\n' + "</rdf:RDF>";

			in.close();

			return finalFile;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";

	}  


	public static String queryMembers(String URI) {

		String memberList = "";

		try{
			edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
			edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();

			String query = "PREFIX pmlp: <http://inference-web.org/2.0/pml-provenance.owl#> SELECT ?member WHERE {?member pmlp:isMemberOf <" + URI + ">}";

			String members = proxy.doQuery(query);

			ResultSet results = ResultSetFactory.fromXML(members);

			//		System.out.println(members);
			//		System.out.println(results);

			if(results != null)
				while(results.hasNext()){

					String member = results.nextSolution().get("?member").toString();

					if(member != null){
						memberList = member + "," + memberList;
					}
				}

		}catch (Exception e){
			e.printStackTrace();
		}
		return memberList;

	}

}

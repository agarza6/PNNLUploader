/**
Copyright (c) 2012, University of Texas at El Paso
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.utep.trust;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PNNLUploader extends HttpServlet {
	private static final long serialVersionUID = 1L;

	ArrayList<String> PML_P = new ArrayList<String>();

	/**
	 * 0 - Header
	 * 1 - Organizations
	 * 2 - Members
	 * 3 - Footer
	 */
	int section = 0;

	//Person Variables
	String personName, personURL, personIsMemberOf;
	boolean bPersonName, bPersonURL, bPersonIsMemberOf;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PNNLUploader() {
		super();
	}

	public void init(ServletConfig config)throws ServletException {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		String _URL;

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		_URL = request.getParameter("URL");

		if(_URL == null){
			out.print("URL IS NEEDED");
		}else{
			captureOrganization("SUPERCLASS", _URL, null);
		}

		for(Iterator<String> iter = PML_P.iterator(); iter.hasNext();){
			out.println(iter.next() + '\n');
		}

	}

	private void captureOrganization(String orgName, String orgURL, String parentOrg){
		String myURI = writeOrgPML(orgName,orgURL,parentOrg);

		String inputLine;
		boolean insidePerson = false, insideOrg = false;

		try{
			URL oracle = new URL(orgURL);
			BufferedReader in = new BufferedReader( new InputStreamReader(oracle.openStream()));

			while ((inputLine = in.readLine()) != null){
				inputLine.trim();

				/**
				 * PARSER
				 */

				//Define Section of Document
				if(inputLine.contains("<table") && section == 0){
					section = 1;
				}else if(inputLine.contains("</table>") && section == 1){
					section = 2;
				}else if(inputLine.contains("</table>") && section == 2){
					section = 3; 
				}


				//Current Organization
				if(inputLine.contains("manager of") && orgName == null){
					orgName = inputLine.substring(inputLine.indexOf("nager of") + 1);
				}

				//Capture subOrganizations
				if(section == 1){
					if(inputLine.contains("<tr>"))
						insideOrg = true;

					if(insideOrg && inputLine.contains("<td><a href=")){
						String subOrgURL = inputLine.substring(inputLine.indexOf("=\"")+ 2, inputLine.indexOf("\">"));
						String subOrgName = inputLine.substring(inputLine.indexOf("\">")+ 2, inputLine.indexOf("</a>"));
						captureOrganization(subOrgName, subOrgURL, myURI);

						while ((inputLine = in.readLine()) != null)
							if(inputLine.contains("</tr>")){
								insideOrg = false;
								break;
							}
					}
				}else if(section == 2){
					if(inputLine.contains("<tr>"))
						insidePerson = true;

					if(insidePerson && inputLine.contains("<td><a href=")){
						personURL = inputLine.substring(inputLine.indexOf("=\"")+ 2, inputLine.indexOf("\">"));
						personName = inputLine.substring(inputLine.indexOf("\">")+ 2, inputLine.indexOf("</a>"));
						capturePerson(personName, personURL, myURI);

						while ((inputLine = in.readLine()) != null)
							if(inputLine.contains("</tr>")){
								insidePerson = false;
								break;
							}
					}
				}

			}

			in.close();


		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String writeOrgPML(String orgName, String orgURL, String isMemberOf){
		
		String shortName = orgName;
		String pmlp_url = "http://URL_OF_SERVER/";
		
		shortName = shortName.replaceAll("[*<>\\[\\]\\+\",]", "-");
		shortName = shortName.replaceAll(" ", "_");

		String pmlP = "<rdf:RDF" + '\n';
		pmlP += '\t' + "xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'" + '\n';
		pmlP += '\t' + "xmlns:pmlp='http://inference-web.org/2.0/pml-provenance.owl#'" + '\n';
		pmlP += '\t' + "xmlns:owl='http://www.w3.org/2002/07/owl#'" + '\n';
		pmlP += '\t' + "xmlns:xsd='http://www.w3.org/2001/XMLSchema#'" + '\n';
		pmlP += '\t' + "xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'>" + '\n';

		pmlP += '\t' + "<pmlp:Organization rdf:about='" + pmlp_url + "pmlp/" + shortName + ".owl" + '#' + shortName + "'>" + '\n';
		pmlP += '\t' + "<pmlp:hasName rdf:datatype='http://www.w3.org/2001/XMLSchema#string'>" + shortName + "</pmlp:hasName>" + '\n';
		pmlP += '\t' + "<pmlp:hasDescription>" + '\n';
		pmlP += "\t\t" + "<pmlp:Information>" + '\n';
		pmlP += "\t\t\t" + "<pmlp:hasURL rdf:datatype='http://www.w3.org/2001/XMLSchema#anyURI'>" + orgURL + "</pmlp:hasURL>" + '\n';
		pmlP += "\t\t" + "</pmlp:Information>" + '\n';
		pmlP += '\t' + "</pmlp:hasDescription>" + '\n';

		//check if member of another Organization
		if(isMemberOf != null && !isMemberOf.isEmpty()){
			pmlP += '\t' + "<pmlp:isMemberOf rdf:resource='" + isMemberOf  + "'/>" + '\n';
		}

		pmlP += '\t' + "</pmlp:Organization>" + '\n';
		pmlP += "</rdf:RDF>";

		//		System.out.println(pmlP);

		PML_P.add(pmlP);
		return pmlp_url + "/" + shortName + ".owl" + '#' + shortName;
	}
	
	private void capturePerson(String name, String URI, String isMemberOf){
		
		String shortName = name;
		String pmlp_url = "http://URL_OF_SERVER/";
		
		shortName = shortName.replaceAll("[*<>\\[\\]\\+\",]", "-");
		shortName = shortName.replaceAll(" ", "_");

		String pml_foaf = "<rdf:RDF" + '\n';
		//Imports
		pml_foaf += '\t' + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" + '\n';
		pml_foaf += '\t' + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" + '\n';
		pml_foaf += '\t' + "xmlns:pmlp=\"http://inference-web.org/2.0/pml-provenance.owl#\"" + '\n';
		pml_foaf += '\t' + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" + '\n';
		pml_foaf += '\t' + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"" + '\n';
		pml_foaf += '\t' + "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"" + '\n';
		pml_foaf += '\t' + "xmlns:admin=\"http://webns.net/mvcb/\">" + '\n';
		//Foaf/PML declarations
		pml_foaf += '\t' + "<foaf:Person rdf:about=\"" + pmlp_url + "pmlp/" + shortName + ".owl" + '#' + shortName + "\">" + '\n';
		pml_foaf += "\t\t" + "<rdf:type rdf:resource=\"http://inference-web.org/2.0/pml-provenance.owl#Person\"/>" + '\n';
		pml_foaf += "\t\t" + "<foaf:primaryTopic rdf:resource=\"#me\"/>" + '\n';
		pml_foaf += "\t\t" + "<admin:generatorAgent rdf:resource=\"http://trust.cs.utep.edu/derivA/\"/>" + '\n';
		pml_foaf += "\t\t" + "<admin:errorReportsTo rdf:resource=\"mailto:agarza6@miners.utep.edu\"/>" + '\n';
		//PML Info
		pml_foaf += "\t\t" + "<pmlp:hasName rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">" + shortName + "</pmlp:hasName>" + '\n';
		if(!isMemberOf.isEmpty() && isMemberOf.length() > 0)
			pml_foaf += "\t\t" + "<pmlp:isMemberOf rdf:resource=\"" + isMemberOf + "\"/>" + '\n';

		//FOAF Info

		pml_foaf += "\t\t" + "<foaf:name>" + shortName + "</foaf:name>" + '\n';

		
		//End File
		pml_foaf += '\t' + "</foaf:Person>" + '\n';
		pml_foaf += "</rdf:RDF>" + '\n';

		PML_P.add(pml_foaf);

	}

	public static void main(String[] args){
		//captureOrganization(null, "http://minas.cs.utep.edu/derivA/pnnltest.html", null);
	}

}

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
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servlet implementation class URLRewriter
 */
public class URLRewriter extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Hashtable hash;
	String hashTableID = "SESSION_HASH_TABLE";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public URLRewriter() {
        super();
    }
    
	public void init(ServletConfig config)throws ServletException {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
//		HttpSession session = request.getSession(true);
//		
//		hash = (Hashtable)session.getAttribute(hashTableID);
//		
//		if(hash == null){
//			hash = new Hashtable(15, 15);
//			session.setAttribute(hashTableID, hash);
//		}
//		
//		String servletURL, smallURL;
		
		String URL;

//		contextPath = request.getContextPath();
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		//	    ServletContext context = getServletContext();

		//====================================================
		// URL/URI
		//====================================================
		URL = request.getParameter("URI");

		if(URL == null){
			out.print("URL IS WANTED");
		}else{
			
			URL oracle = new URL(URL);
			String inputLine;
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							oracle.openStream()));

			while ((inputLine = in.readLine()) != null){

				if(inputLine.contains("<pmlp:Organization rdf:about=")){
					//Example of line: <pmlp:Organization rdf:about='http://rio.cs.utep.edu/ciserver/ciprojects/pmlp/JPL.owl#JPL'>
					out.print(inputLine.substring(inputLine.indexOf('=') + 2, inputLine.lastIndexOf('>') - 1));
				}

				if(inputLine.contains("</pmlp:Organization>")){
					break;
				}
			}
			
		}
		
	}

	
	public static void main(String[] args){
		
		try {
			 
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("mappings");
			doc.appendChild(rootElement);
	 
			// staff elements
			Element mapping = doc.createElement("mapping");
			rootElement.appendChild(mapping);
	 
			// set attribute to staff element
			Element from = doc.createElement("map_From");
			from.appendChild(doc.createTextNode("FROM HERE"));
			mapping.appendChild(from);

			// set attribute to staff element
			Element to = doc.createElement("map_To");
			to.appendChild(doc.createTextNode("TO HERE"));
			mapping.appendChild(to);
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
//			StreamResult result = new StreamResult(new File("D:\\test\\mappings.xml"));
			StreamResult result = new StreamResult(new File("http://localhost:8080/OrgWithMembers/mappings.xml"));
	 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
	 
		  } catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		  } catch (TransformerException tfe) {
			tfe.printStackTrace();
		  }
		
	}
	
}

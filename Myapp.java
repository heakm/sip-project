
/*
 * $Id: EchoServlet.java,v 1.5 2003/06/22 12:32:15 fukuda Exp $
 */
package org.mobicents.servlet.sip.example;

import java.util.*;
import java.io.IOException;

import javax.servlet.sip.SipServlet;	
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.ServletException;
import javax.servlet.sip.URI;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipFactory;

/**
 */
public class Myapp extends SipServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static private Map<String, String> RegistrarDB;
	static private Map<String, String> StateDB;
	static private SipFactory factory;
	
	public Myapp() {
		super();
		RegistrarDB = new HashMap<String,String>();
		StateDB =  new HashMap<String,String>();
	}
	
	public void init() {
		factory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);
	}

	/**
        * Acts as a registrar and location service for REGISTER messages
        * @param  request The SIP message received by the AS 
        */
	protected void doRegister(SipServletRequest request) throws ServletException,
			IOException {
		/*String aor = getSIPuri(request.getHeader("To"));
		String contact = getSIPuriPort(request.getHeader("Contact"));
		RegistrarDB.put(aor, contact);
		SipServletResponse response; 
		response = request.createResponse(200);
		response.send();
		
	    // Some logs to show the content of the Registrar database.
		log("REGISTER (myapp):***");
		Iterator<Map.Entry<String,String>> it = RegistrarDB.entrySet().iterator();
    		while (it.hasNext()) {
        		Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
        		System.out.println(pairs.getKey() + " = " + pairs.getValue());
    		}
		log("REGISTER (myapp):***");
		*/

		/*****************************************NUEVO CODIGO********************************************** */
		SipServletResponse response; 
		
		
		String aor = getSIPuri(request.getHeader("To")); // Direccion de la BBDD
		String contact = (request.getHeader("Contact")); // El contacto que queremos meter en la BBDD
		// contact tiene un formato tipo <sip:name@domain.pt>;expires=1
		// separamos en dos por la @ --> cont[1] = domain.pt>;expires=3600
		String[] cont  =  aor.split("@"); 
		// Extraemos el grupo separando en dos por el ">" --> cont2[0] = domain.pt
		String[] cont2 = cont[1].split(">"); 
		String group = cont2[0];
		if (group.equals("acmet.pt")){
			log(contact);
			String[] expire = contact.split("=");
			log(expire[1]);
			int exp = Integer.parseInt(expire[1]);
			//log(new String (exp));
			if(exp == 0){ // We do the (De)Register
				RegistrarDB.remove(aor);
				StateDB.remove(aor);
				//StateDB.put(aor,"NON-REGISTRADO");
				response = request.createResponse(200);
				response.send();
			}else{
				RegistrarDB.put(aor, getSIPuriPort(contact));
				StateDB.put(aor,"REGISTRADO");
				response = request.createResponse(200);
				response.send();
			}

			// Some logs to show the content of the Registrar database.
				log("REGISTER (myapp):***");
				Iterator<Map.Entry<String,String>> it = RegistrarDB.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
					System.out.println(pairs.getKey() + " = " + pairs.getValue());
				}
				Iterator<Map.Entry<String,String>> i = StateDB.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry<String,String> pairs = (Map.Entry<String,String>)i.next();
					System.out.println(pairs.getKey() + " = " + pairs.getValue());
				}
				log("REGISTER (myapp):***");
		}else{
			response = request.createResponse(403);
			response.send();
		}
	
	}

	/**
        * Sends SIP replies to INVITE messages
        * - 300 if registred
        * - 404 if not registred
        * @param  request The SIP message received by the AS 
        */
	protected void doInvite(SipServletRequest request)
                  throws ServletException, IOException{ 
		
		
		// Some logs to show the content of the Registrar database.
		log("INVITE (myapp):***");
			Iterator<Map.Entry<String,String>> it = RegistrarDB.entrySet().iterator();
    		while (it.hasNext()) {
        		Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
        		System.out.println(pairs.getKey() + " = " + pairs.getValue());
    		}
			Iterator<Map.Entry<String,String>> i = StateDB.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<String,String> pairs = (Map.Entry<String,String>)i.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
			}
		log("INVITE (myapp):***");


		// Si estamos en una llamada en cuanto se inicia la llamada los dos lados de la llamada cambain el estado a OCUPADO --> ¿Cómo sabemos cuando coje la llamada?
		// Cuando uno de los dos mande un BYE los dos pasaran a estado LIBRE o REGISTRADO 
		// Si la llamada se cuelga antes de que se coja, analizamos el mesjae CANCEL y cmabiamos el estado a LIBRE o REGISTRADO
		// ¿Si no coje el telefono?

		// Si estamos en una conferencia en cuanto se inicia, se cambia a OCUPADO
		// Cuando la conferencia finaliza y se manda un BYE, se cambia el estado del q manda el BYE a LIBRE o REGISTRADO



		
		String aor = getSIPuri(request.getHeader("To")); // Get the To AoR
		String domain = aor.substring(aor.indexOf("@")+1, aor.length());
		String contact = getSIPuri(request.getHeader("From")); // Get the From AoR
		String domain1 = contact.substring(contact.indexOf("@")+1, contact.length());
		log(domain + "  " + domain1);
		if(domain1.equals("acmet.pt")){ // FROM
			if(domain.equals("acmet.pt")){ // TO
				SipServletResponse response; 
				response = request.createResponse(100); // It is sending the invite the proxy (Provisional message)
				response.send();
				String[] names = aor.split(":");
				log("YPRIMERA DIVISON " + names[1]);
				String[] name = names[1].split("@");

				log("You are calling for the user whose name is " + name[0]);
				// Conference
				if(name[0].equals("chat")){ //  it will conect to the conference only if the user is registered

					log("HEMOS MANDADO LA SOLICITUD DE CONFERENCIA");
					Proxy proxy = request.getProxy();
					proxy.setRecordRoute(false);
					proxy.setSupervised(false);
					URI toContact = factory.createURI("sip:chat@127.0.0.1:5070");
					proxy.proxyTo(toContact);
					StateDB.put(contact,"IN-CONFERENCE");
				/*
				// Indirect Sessions
				}else if(name[0].equals("gofind")){
					if(!StateDB.containsKey(contact)){ // it isn't registered
						response = request.createResponse(200);
						response.send();
						// MANDAR un mensaje SIP indicando q el contenido es NON REGISTER o IN CONFERENCE OR BUSY
						response = request.createResponse(409); // Existe un conflicto
						response.send();
					}
					else{
						String state = StateDB.get(contact);
						if(state.equals("NON-REGISTRED") || state.equals("IN-CONFERENCE") || state.equals("BUSY")){
							response = request.createResponse(200);
							response.send();
							// MANDAR un mensaje SIP indicando q el contenido es NON REGISTER o IN CONFERENCE OR BUSY
							response = request.createResponse(409);
							response.send();
						}else{

						}
					}
				*/
				}else if(!RegistrarDB.containsKey(aor)){
					response = request.createResponse(404);
					response.send();

				// Direct session
				}else{
					Proxy proxy = request.getProxy();
					proxy.setRecordRoute(false);
					proxy.setSupervised(false);
					URI toContact = factory.createURI(RegistrarDB.get(aor));
					proxy.proxyTo(toContact);
					StateDB.put(aor,"BUSY");
					StateDB.put(contact,"BUSY");
				}
					
				
			}else{
				log("/n/n/n/nTESTING DONT BELONG-->TO" + domain + "/n/n/n/n");
				SipServletResponse response; 
				response = request.createResponse(404);
				response.send();
			}
		}else{
			SipServletResponse response; 
			response = request.createResponse(403);
			response.send();
		}
		log("INVITE (myapp):***");
			Iterator<Map.Entry<String,String>> it2 = RegistrarDB.entrySet().iterator();
    		while (it2.hasNext()) {
        		Map.Entry<String,String> pairs = (Map.Entry<String,String>)it2.next();
        		System.out.println(pairs.getKey() + " = " + pairs.getValue());
    		}
			Iterator<Map.Entry<String,String>> i2 = StateDB.entrySet().iterator();
			while (i2.hasNext()) {
				Map.Entry<String,String> pairs = (Map.Entry<String,String>)i2.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
			}
		log("INVITE (myapp):***");
	}

	protected void doMessage(SipServletRequest request)
                  throws ServletException, IOException{ 
		log("Estoy en doMessage");

		String aor = getSIPuri(request.getHeader("To")); // Get the To AoR --> gofind@acmet.pt
		String contact = getSIPuri(request.getHeader("From")); // Get the From AoR --> Alice or Trudy
		log("The to header "+ aor);
		String contactDB = contact;
		contact = RegistrarDB.get(contact);
		log("The from header "+ contact);
		String domain = aor.substring(aor.indexOf("@")+1, aor.length()); // --> Extrat the destination group
		log(domain);
		if(domain.equals("acmet.pt")){ // --> Check that the destination gorup is acmet.pt
			String[] names = aor.split(":");
			String[] name = names[1].split("@");
			log("You are calling for the user whose name is " + name[0]);
			if(name[0].equals("gofind")){ // We are going to accept only request to gofind@acmet.pt
				Object contenido = request.getContent(); // Extract the content of the message, it must be a person that the origin wants to call
				String content = contenido.toString(); // Pass it to a string del tipo --> XXXX@YYYYY.ZZ
				log("The destination name an domain is: " + content);
				String sip = "sip:";
				content = sip + content;
				log("The destination name an domain is: " + content);
				if(RegistrarDB.containsKey(content)){ // The destination is registered --> No me está reconociendo cuando trudy está registrada
					request.createResponse(200).send();
					String contentCompleteDirection = RegistrarDB.get(content);
					log("The to header "+ aor);
					log("The from header " + contact);
					log("The message content " + content); 
					log("The message content complete direction " + contentCompleteDirection); 
					String state = StateDB.get(content);
					log("El estado de la direccion del contenido es: "+ state);
					if(state.equals("FREE") || state.equals("REGISTRADO")){
						log("We can stablish the call... we send the invites");
						SipServletRequest myMessage = factory.createRequest(request.getApplicationSession(),"INVITE",content,contact);
						SipServletRequest myMessage2 = factory.createRequest(request.getApplicationSession(),"INVITE",contact, contentCompleteDirection);
						
						myMessage2.send();
						myMessage.send();

						StateDB.put(content,"BUSY");
						StateDB.put(contactDB,"BUSY");

						log("INVITE (myapp):***");
							Iterator<Map.Entry<String,String>> it2 = RegistrarDB.entrySet().iterator();
							while (it2.hasNext()) {
								Map.Entry<String,String> pairs = (Map.Entry<String,String>)it2.next();
								System.out.println(pairs.getKey() + " = " + pairs.getValue());
							}
							Iterator<Map.Entry<String,String>> i2 = StateDB.entrySet().iterator();
							while (i2.hasNext()) {
								Map.Entry<String,String> pairs = (Map.Entry<String,String>)i2.next();
								System.out.println(pairs.getKey() + " = " + pairs.getValue());
							}
						log("INVITE (myapp):***");
					}else{
						log("We CAN'T stablish the call");
						SipServletRequest myMessage = factory.createRequest(request.getApplicationSession(),"MESSAGE",aor,contact);
						String myText = "The destination is not available";
						myMessage.setContent((Object) myText,"text/plain");
						myMessage.send();
					}
					
				}else{ // we are going to supose that they always send xxxx@YYYY.ZZ
					String domainDestination = content.substring(content.indexOf("@")+1, content.length());
					log("Destination domain = " + domainDestination);
					if(domainDestination.equals("acmet.pt")){ // Belongs to the group but it is not registered
						request.createResponse(200).send();
						SipServletRequest myMessage = factory.createRequest(request.getApplicationSession(),"MESSAGE",aor,contact);
						String myText = "The destination it is NON-REGISTERED";
						myMessage.setContent((Object) myText,"text/plain"); // No le gusta el content type
						myMessage.send();
					}else{ // the destination do not belongs to the group
						log("The message do not contain a destination of the group acmet.pt");
						request.createResponse(403).send();
					}
				}
				
			}else{
				log("The destination it is not gofind");
				request.createResponse(404).send();
			}
		}else{
			request.createResponse(403).send();
		}
		
	}
	/*
	protected void doResponse(SipServletRequest request)
                  throws ServletException, IOException{ 
			
	
	
	
	}
	*/

	protected void doSuccessResponse(SipServletResponse response)
                  throws ServletException, IOException{ 
		
		SipServletRequest request = response.getRequest();
		
		String typeMessage = request.getHeader("Cseq");
		String typeMessage2 = response.getHeader("Cseq"); // Si contiene un INVITE es que está contestando a un invite
		String contactPort = getSIPuriPort(request.getHeader("Contact"));
		log(contactPort);
		if(typeMessage2.contains("INVITE") && contactPort.contains("5060")){
			Object content = response.getContent();
			String aor = getSIPuriPort(request.getHeader("To"));
			String from = getSIPuri(request.getHeader("From"));
			SipServletRequest myMessage = factory.createRequest(request.getApplicationSession(),"MESSAGE",from,aor);
			myMessage.setContent(content,"text/plain"); // No le gusta el content type
			myMessage.send();
			log("I have change my state in the indirect session");
		}
		SipServletRequest ACK = response.createAck();
		ACK.send();
	
	}


	protected void doBye(SipServletRequest request)
                  throws ServletException, IOException{ 
			
		String aor = getSIPuri(request.getHeader("To")); // Get the To AoR	
		String contact = getSIPuri(request.getHeader("From")); // Get the From AoR
		String[] division = contact.split("@");
		contact = division[0] + "@acmet.pt";
		if(aor.contains("chat")){
			StateDB.put(contact,"FREE");
		}else{
			StateDB.put(aor,"FREE");
			StateDB.put(contact,"FREE");
		}
		request.createResponse(200).send();

		log("BYE (myapp):***");
			Iterator<Map.Entry<String,String>> it = RegistrarDB.entrySet().iterator();
    		while (it.hasNext()) {
        		Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
        		System.out.println(pairs.getKey() + " = " + pairs.getValue());
    		}
			Iterator<Map.Entry<String,String>> i = StateDB.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry<String,String> pairs = (Map.Entry<String,String>)i.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
			}
		log("BYE (myapp):***");

	}
	protected void doCancel(SipServletRequest request)
                  throws ServletException, IOException{ 

		String aor = getSIPuri(request.getHeader("To")); // Get the To AoR	
		String contact = getSIPuri(request.getHeader("From")); // Get the From AoR
		StateDB.put(aor,"FREE");
		StateDB.put(contact,"FREE");

		log("CANCEL (myapp):***");
		Iterator<Map.Entry<String,String>> it = RegistrarDB.entrySet().iterator();
    	while (it.hasNext()) {
       		Map.Entry<String,String> pairs = (Map.Entry<String,String>)it.next();
       		System.out.println(pairs.getKey() + " = " + pairs.getValue());
    	}
		Iterator<Map.Entry<String,String>> i = StateDB.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String,String> pairs = (Map.Entry<String,String>)i.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}
		log("CANCEL (myapp):***");
	}
	/**
        * Auxiliary function for extracting SPI URIs
        * @param  uri A URI with optional extra attributes 
        * @return SIP URI 
        */
	protected String getSIPuri(String uri) {
		String f = uri.substring(uri.indexOf("<")+1, uri.indexOf(">"));
		int indexCollon = f.indexOf(":", f.indexOf("@"));
		if (indexCollon != -1) {
			f = f.substring(0,indexCollon);
		}
		return f;
	}

	/**
        * Auxiliary function for extracting SPI URIs
        * @param  uri A URI with optional extra attributes 
        * @return SIP URI and port 
        */
	protected String getSIPuriPort(String uri) {
		String f = uri.substring(uri.indexOf("<")+1, uri.indexOf(">"));
		return f;
	}
}

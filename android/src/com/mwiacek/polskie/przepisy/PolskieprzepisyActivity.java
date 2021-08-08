package com.mwiacek.polskie.przepisy;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;

public class PolskieprzepisyActivity extends Activity {
    StringBuilder s= new StringBuilder(),poczatek= new StringBuilder(),koniec= new StringBuilder();
    StringBuilder lev= new StringBuilder();
	ScrollerTextViewMain textview;        
	ScrollerTextViewMovable scroller1;
	ScrollerWebViewMovable scroller2;	
	ScrollerWebViewMain webview;        
	InputStream stream;
	Spinner spinner;
	Document document;
	ProgressDialog pd = null;
	final Activity MyActivity = this;    
    SharedPreferences sp;
    String opis;    
    
	private void aktualizuj(Node node,String datajednolita, String akt) {
		if (node.getNodeType()==Node.ELEMENT_NODE) {
		//	NamedNodeMap nm = node.getAttributes();

			if (node.getNodeName().equals("akt-prawny")) {
			
				opis = opis+", "+node.getAttributes().getNamedItem("opis-aktu").getNodeValue()+" (og³oszony "+node.getAttributes().getNamedItem("data-ogloszenia").getNodeValue()+")";
			opis="cos";	
			} else if (node.getNodeName().equals("zastap")) {
				if (!node.getAttributes().getNamedItem("w-ustawie").getNodeValue().equals(akt)) {
					return;				
				}
				
				Node welemencie = null, element = null,elementy=null,elementem=null,tekst=null,tekstem=null;
				NodeList listChilds = node.getChildNodes();
				for (int j = 0; j < listChilds.getLength(); j++) {		
					if (listChilds.item(j).getNodeType()!=Node.ELEMENT_NODE) {
						continue;
					}
					if (listChilds.item(j).getNodeName().equals("element")) {
						element=listChilds.item(j);
					} else if (listChilds.item(j).getNodeName().equals("elementy")) {
						elementy=listChilds.item(j);
					} else if (listChilds.item(j).getNodeName().equals("elementem")) {
						elementem=listChilds.item(j);
					} else if (listChilds.item(j).getNodeName().equals("tekst")) {
						tekst=listChilds.item(j);
					} else if (listChilds.item(j).getNodeName().equals("tekstem")) {
						tekstem=listChilds.item(j);
					} else if (listChilds.item(j).getNodeName().equals("w-elemencie")) {
						welemencie=listChilds.item(j);
					}
				}
				if (element!=null && elementem!=null) {
					XPath xpath = XPathFactory.newInstance().newXPath();
					try {
						NodeList nodes = (NodeList) xpath.evaluate(element.getAttributes().getNamedItem("id").getNodeValue(), document, XPathConstants.NODESET);
						if (nodes.getLength()!=1) {
//							Log.d("problem","zastap - ilosc elementow rozna od 1");
							return;
						}
						Node oldentry = nodes.item(0);
			//			Log.d("stary",oldentry.getNodeName());
						if (oldentry==null) {
							//Log.d("ok","zle old");
							
						}
						Node newentry = elementem;
						//Node newentry = document.importNode(elementem, true);
		//				Log.d("nowy",newentry.getNodeName());
						if (newentry==null) {
							//Log.d("ok","zle new");
							
						}
						//Log.d("ok","zastepuje 1");
						Node p = oldentry.getParentNode(); 
						p.replaceChild(document.adoptNode(elementem),oldentry);						
					} catch (XPathExpressionException e) {
					}
				} else if (welemencie!=null && tekst!=null && tekstem!=null) {
					XPath xpath = XPathFactory.newInstance().newXPath();
					try {
						NodeList nodes = (NodeList) xpath.evaluate(welemencie.getAttributes().getNamedItem("id").getNodeValue(), document, XPathConstants.NODESET);
						if (nodes.getLength()!=1) {
							//Log.d("problem","zastap 2 - ilosc elementow rozna od 1");
							return;
						}
						//Log.d("zastap 2","-"+tekstem.getChildNodes().item(0).getTextContent()+"-");
						//Log.d("zastap 22","-"+tekst.getChildNodes().item(0).getTextContent()+"-");
						Boolean znalazl=false;
						for (int j = 0; j < nodes.item(0).getChildNodes().getLength(); j++) {
							if (nodes.item(0).getChildNodes().item(j).getNodeType()!=Node.TEXT_NODE) {
								continue;
							}
							String s = nodes.item(0).getChildNodes().item(j).getTextContent().replace("\r", " ").replace("\n", " ").trim();
							//Log.d("zastap 23","-"+s+"-");							
							String s2 = s.replace(tekst.getChildNodes().item(0).getTextContent(),tekstem.getChildNodes().item(0).getTextContent());
							if (!s.equals(s2)) {
								znalazl=true;
								nodes.item(0).getChildNodes().item(j).setTextContent(s2);
								break;
							}
						}						
						if (!znalazl) {
							//Log.d("problem","zastap 2 - nie znaleziono elementu do zastapienia");
							return;
						}
					} catch (XPathExpressionException e) {
					}
					
				} else {
					//Log.d("problem","zastap - brak znanego wariantu");
				}
			}			
		}

		NodeList listChilds = node.getChildNodes();
		for (int j = 0; j < listChilds.getLength(); j++) {		
			if (listChilds.item(j).getNodeType()!=Node.TEXT_NODE) {
				aktualizuj(listChilds.item(j),datajednolita,akt);
			}
		}
	}	
    
	private void pokaztxt(Node node, Boolean first, Boolean firstlevel) {
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			NamedNodeMap nm = node.getAttributes();

			s.append(koniec);
			koniec.delete(0, koniec.length());
			
			if (node.getNodeName().equals("akt-prawny")) {
			
				opis = "Wersja ujednolicona automatycznie na podstawie\n"+nm.getNamedItem("opis-aktu").getNodeValue()+" (og³oszony "+nm.getNamedItem("data-ogloszenia").getNodeValue()+")";

			} else if (node.getNodeName().equals("ustawa")) {
				s.append("Ustawa z dnia "+nm.getNamedItem("data-wydania").getNodeValue()+"\n");
			} else if (node.getNodeName().equals("tytul")) {
				s.append(node.getTextContent()+"\n\n");				
//				echo "<p><center><b>".getText2($node,true)."</b></center>";
				
				//echo "<p><center><b>".getText2($node,true)."</b></center>";
				return;
			} else if (node.getNodeName().equals("tekst") || node.getNodeName().equals("tekstem")) {
				poczatek.delete(0, poczatek.length());
				return;
			} else if (node.getNodeName().equals("artykul")) {
				s.append("\n\n"+poczatek+"Art. "+nm.getNamedItem("nr").getNodeValue()+" ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("ustep")) {
				if (!first) {
					s.append("\n\n");
				}
				s.append(poczatek+nm.getNamedItem("nr").getNodeValue()+". ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("punkt")) {
				if (!firstlevel) {
					lev.append("  ");					
				}
				s.append("\n\n"+lev+poczatek+nm.getNamedItem("nr").getNodeValue()+") ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("litera")) {
				if (!firstlevel) {
					lev.append("  ");					
				}
				s.append("\n\n"+lev+poczatek+nm.getNamedItem("nr").getNodeValue()+". ");

				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("tiret")) {
				if (!firstlevel) {
					lev.append("  ");				
				}
				s.append("\n\n"+lev+"- "+poczatek);

				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("wciecie")) {
				if (!firstlevel) {
					lev.append("  ");					
				}
				s.append("\n\n\""+lev+poczatek);
			} else if (node.getNodeName().equals("tresc-przypisu")) {
//				if (getAttribute($node,"nr")=="1") {
					//echo "<hr>";
				//}
				//echo "<p><a name=przypis".getAttribute($node,"nr").">".getAttribute($node,"nr").") ";
			} else if (node.getNodeName().equals("podpisy")) {
//				echo "<p>".getText2($node,true);
				return;
			} else if (node.getNodeName().equals("zastap")) {
				s.append("\n\n");
				lev.append("  ");					
				poczatek.append("\"");
			} else if (node.getNodeName().equals("dodaj")) {
				s.append("\n\n");
				lev.append("  ");					
				poczatek.append("\"");
			}			
		}

		Boolean flevel=false;
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			if (node.getNodeName().equals("element") || node.getNodeName().equals("elementem")) {
				flevel=true;				
			}
		}
		Boolean f=true;
		
		NodeList listChilds = node.getChildNodes();
		for (int j = 0; j < listChilds.getLength(); j++) {		
			if (listChilds.item(j).getNodeType()==Node.TEXT_NODE) {
			//	s.append("ble");
				s.append(poczatek+listChilds.item(j).getTextContent().replace("\r", " ").replace("\n", " ").trim());
				poczatek.delete(0, poczatek.length());
				flevel=false;
			} else {
				pokaztxt(listChilds.item(j),f,flevel);
			}
			f=false;
		}
		
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			if (node.getNodeName().equals("zastap") || node.getNodeName().equals("dodaj")) {
				s.append("\"");				
			}
			if (node.getNodeName().equals("punkt") || node.getNodeName().equals("litera") ||
				node.getNodeName().equals("tiret") || node.getNodeName().equals("wciecie") ||
				node.getNodeName().equals("dodaj") || node.getNodeName().equals("zastap")) {
				if (!firstlevel) {
					lev.delete(0, 2);
				}
			}
		}
	}

	private void pokazweb(Node node, Boolean first, Boolean firstlevel) {
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			NamedNodeMap nm = node.getAttributes();

			s.append(koniec);
			koniec.delete(0, koniec.length());
			
			if (node.getNodeName().equals("akt-prawny")) {
			
				opis = "Wersja ujednolicona automatycznie na podstawie "+nm.getNamedItem("opis-aktu").getNodeValue()+" (og³oszony "+nm.getNamedItem("data-ogloszenia").getNodeValue()+")";
			} else if (node.getNodeName().equals("ustawa")) {
				s.append("<center>Ustawa z dnia "+nm.getNamedItem("data-wydania").getNodeValue()+"</center>");
			} else if (node.getNodeName().equals("tytul")) {
				s.append("<p><center><b>"+node.getTextContent()+"</b></center>");				

				return;
			} else if (node.getNodeName().equals("tekst") || node.getNodeName().equals("tekstem")) {
				poczatek.delete(0, poczatek.length());
				return;
			} else if (node.getNodeName().equals("artykul")) {
				s.append("<p><b>"+poczatek+"Art. "+nm.getNamedItem("nr").getNodeValue()+"</b> ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("ustep")) {
				if (!first) {
					s.append("<p>");
				}
				s.append(poczatek+nm.getNamedItem("nr").getNodeValue()+". ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("punkt")) {
				if (!firstlevel) {
					s.append("<div style='margin-left:15px'>");					
				}
				s.append("<p>"+poczatek+nm.getNamedItem("nr").getNodeValue()+") ");
				poczatek.delete(0, poczatek.length());
				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("litera")) {
				if (!firstlevel) {
					s.append("<div style='margin-left:15px'>");										
				}
				s.append("<p>"+poczatek+nm.getNamedItem("nr").getNodeValue()+". ");

				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("tiret")) {
				if (!firstlevel) {
					s.append("<div style='margin-left:15px'>");										
				}
				s.append("<p>- "+poczatek);

				if (nm.getNamedItem("uchylony")!=null && nm.getNamedItem("uchylony").getNodeValue().equals("tak")) {
					s.append(" (uchylony)");
					return;
				}
			} else if (node.getNodeName().equals("wciecie")) {
				if (!firstlevel) {
					s.append("<div style='margin-left:15px'>");										
				}
				s.append("<p>- "+poczatek);
			} else if (node.getNodeName().equals("tresc-przypisu")) {
				if (nm.getNamedItem("nr").getNodeValue().equals("1")) {
					s.append("<hr>");
				}
				s.append("<p><a name=przypis"+nm.getNamedItem("nr").getNodeValue()+">"+nm.getNamedItem("nr").getNodeValue()+") ");				
			} else if (node.getNodeName().equals("podpisy")) {
				s.append("<p>"+node.getTextContent().replace("\r", " ").replace("\n", " ").trim());
				return;
			} else if (node.getNodeName().equals("zastap")) {
				s.append("<p><div style='margin-left:15px'>");										
				poczatek.append("\"");
			} else if (node.getNodeName().equals("dodaj")) {
				s.append("<p><div style='margin-left:15px'>");										
				poczatek.append("\"");
			}			
		}

		Boolean flevel=false;
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			if (node.getNodeName().equals("element") || node.getNodeName().equals("elementem")) {
				flevel=true;				
			}
		}
		Boolean f=true;
		
		NodeList listChilds = node.getChildNodes();
		for (int j = 0; j < listChilds.getLength(); j++) {		
			if (listChilds.item(j).getNodeType()==Node.TEXT_NODE) {
				s.append(poczatek+listChilds.item(j).getTextContent().replace("\r", " ").replace("\n", " ").trim());
				poczatek.delete(0, poczatek.length());
				flevel=false;
			} else {
				pokazweb(listChilds.item(j),f,flevel);
			}
			f=false;
		}
		
		if (node.getNodeType()==Node.ELEMENT_NODE) {
			if (node.getNodeName().equals("zastap") || node.getNodeName().equals("dodaj")) {
				s.append("\"");				
			}
			if (node.getNodeName().equals("punkt") || node.getNodeName().equals("litera") ||
				node.getNodeName().equals("tiret") || node.getNodeName().equals("wciecie") ||
				node.getNodeName().equals("dodaj") || node.getNodeName().equals("zastap")) {
				if (!firstlevel) {
					koniec.append("</div>");
				}
			}
		}
	}
	
	public void Pokaz() {
    	pd = ProgressDialog.show(this,"","£adowanie pliku 1",true,false);

    	new Thread(new Runnable(){
    		public void run(){
    			opis="";
    	    	try {
    	    		Log.d("cos",Integer.toString(spinner.getSelectedItemPosition()));
    	    		if (spinner.getSelectedItemPosition()<2) {
        				MyActivity.runOnUiThread(new Runnable() {
        		    		public void run() {    				
        		    			pd.setMessage("£adowanie pliku 1");
        		    		}
        		    	});		 				    			    			
    	    			stream = MyActivity.getAssets().open("1.xml");
    	    			Log.d("cos",Integer.toString(spinner.getSelectedItemPosition()));
        	    		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        	    		builderFactory.setNamespaceAware(false);
        	    		DocumentBuilder builder = builderFactory.newDocumentBuilder();
        	    		document = builder.parse(stream);
        				stream.close();
    	    	    	    	    			
    	    		} else if (spinner.getSelectedItemPosition()==2) {
        				MyActivity.runOnUiThread(new Runnable() {
        		    		public void run() {    				
        		    			pd.setMessage("£adowanie pliku 2");
        		    		}
        		    	});		 				    			    			
    	    			stream = MyActivity.getAssets().open("2.xml");    	    			
        	    		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        	    		builderFactory.setNamespaceAware(false);
        	    		DocumentBuilder builder = builderFactory.newDocumentBuilder();
        	    		document = builder.parse(stream);
        				stream.close();
    	    		} else if (spinner.getSelectedItemPosition()==3) {
        				MyActivity.runOnUiThread(new Runnable() {
        		    		public void run() {    				
        		    			pd.setMessage("£adowanie pliku 3");
        		    		}
        		    	});		 				    			    			
    	    			
    	    			stream = MyActivity.getAssets().open("3.xml");    	    			
        	    		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        	    		builderFactory.setNamespaceAware(false);
        	    		DocumentBuilder builder = builderFactory.newDocumentBuilder();
        	    		document = builder.parse(stream);
        				stream.close();
    	    		}

    	    		if (spinner.getSelectedItemPosition()==0) {
        				MyActivity.runOnUiThread(new Runnable() {
        		    		public void run() {    				
        		    			pd.setMessage("Aktualizacja plikiem 2");
        		    		}
        		    	});		 				    			    			        		    			
        				stream = MyActivity.getAssets().open("2.xml");
        				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        		        builderFactory.setNamespaceAware(false);
        		        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        		        Document document2 = builder.parse(stream);
        		    	aktualizuj(document2,"20120202", "http://dziennikustaw.gov.pl/DU/2011/s/30/151/1");
        				stream.close();
    	    		
        				MyActivity.runOnUiThread(new Runnable() {
        		    		public void run() {    				
        		    			pd.setMessage("Aktualizacja plikiem 3");
        		    		}
        		    	});		 				    			    			        			
        				stream = MyActivity.getAssets().open("3.xml");
        		        builderFactory = DocumentBuilderFactory.newInstance();
        		        builderFactory.setNamespaceAware(false);
        		        builder = builderFactory.newDocumentBuilder();
        		        document2 = builder.parse(stream);
        		    	aktualizuj(document2,"20120202", "http://dziennikustaw.gov.pl/DU/2011/s/30/151/1");
        				stream.close();        				
    	    		}
    			} catch (IOException e) {        				
    			} catch (SAXException e) {
    				e.printStackTrace();
    			} catch (ParserConfigurationException e) {
    				e.printStackTrace();    	    		
    			}
    	    	
				MyActivity.runOnUiThread(new Runnable() {
		    		public void run() {    				
		    			pd.setMessage("Renderowanie");
		    		}
		    	});		 				    			    			

				lev.delete(0, lev.length());
				s.delete(0, s.length());
				
		        if (sp.getBoolean("Prosty", false)) {
    		        pokaztxt(document,true,true);
    				MyActivity.runOnUiThread(new Runnable() {
    		    		public void run() {
    		    			textview.setText(opis+s);
    		    		}
    		    	});	
    				
		        } else {
    		        pokazweb(document,true,true);
    				MyActivity.runOnUiThread(new Runnable() {
    		    		public void run() {    	    		        
    			            webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    			            webview.getSettings().setAppCacheEnabled(false);
    			            webview.setWebViewClient(new WebViewClient());    	
    			            webview.getSettings().setSupportZoom(true);    
    			        	webview.getSettings().setJavaScriptEnabled (false); 

    			            int scale = (int)(100 * webview.getScale());
    		    	        webview.loadDataWithBaseURL("file:///android_asset/","<center>"+opis+"</center><hr>"+s.toString(), "text/html", "utf-8", null);    	        	
    			       		webview.setInitialScale( scale );    		        	
    		    		}
    		    	});	
		        }   
	    		pd.dismiss();	    		        
    			
    		}
        }).start();  		
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
//        if (sp.getBoolean("Kodeks_szukanie", true) || 
 //       	sp.getBoolean("Taryfikator_szukanie", true) ||
        	//sp.getBoolean("Znaki_szukanie", true) ||
        	//sp.getBoolean("KodyUN_szukanie", true) ||
        	//sp.getBoolean("KierPoj_szukanie", true)) {
        	//menu.getItem(1).setEnabled(true);
        //} else {        		
            //menu.getItem(1).setEnabled(false);
        //}    	
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.sett:
        	ShowSett();
            return true;           
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    void ShowSett() {    	
        startActivity(new Intent(this,PreferencesActivity.class));   
    }    
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp=PreferenceManager.getDefaultSharedPreferences(this);
                
        if (sp.getBoolean("Prosty", false)) {
        	setContentView(R.layout.main_txt);

        	spinner = (Spinner) findViewById(R.id.spinner1);         
        	textview = (ScrollerTextViewMain) findViewById(R.id.textView1);        
        	scroller1 = (ScrollerTextViewMovable) findViewById(R.id.view1);
        	scroller1.textview=textview;
        	textview.sv=scroller1;
        
        	ViewGroup.LayoutParams params = scroller1.getLayoutParams();
        	params.width = scroller1.myBitmap.getWidth();
        	scroller1.setLayoutParams(params);
        } else {
        	setContentView(R.layout.main_web);

        	spinner = (Spinner) findViewById(R.id.spinner2);         
        	webview = (ScrollerWebViewMain) findViewById(R.id.webView2);        
        	scroller2 = (ScrollerWebViewMovable) findViewById(R.id.view2);
        	scroller2.webview=webview;
        	webview.sv=scroller2;
        
        	ViewGroup.LayoutParams params = scroller2.getLayoutParams();
        	params.width = scroller2.myBitmap.getWidth();
        	scroller2.setLayoutParams(params);        	
        }

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(        		
                this, R.array.pliki, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(R.layout.pliki_spinner);
        spinner.setAdapter(adapter1);        
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            	Pokaz();
            } 
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            } 
        });                     
        
      //  Pokaz();
    }
}
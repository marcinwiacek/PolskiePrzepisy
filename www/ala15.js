var ForReading = 1, ForWriting = 2, ForAppending = 8;

var fso = new ActiveXObject("Scripting.FileSystemObject");

//var filename = "d2011030015101.txt";
//var filename = "D20110~2.TXT";
//var filename = "D20120~1.TXT ";
var filename = "D19970~1.TXT ";


String.prototype.LTrim = function () {
    var whitespace = new RegExp("^\\s+","gm");
    return this.replace(whitespace, "");
}

String.prototype.RTrim = function () {
//	return this;
	var whitespace = new RegExp("\\s+$","gm");
	return this.replace(whitespace, "");
}

var f = fso.OpenTextFile(filename, ForReading);
var f2 = fso.CreateTextFile(filename+"2");
//f3 = fso.CreateTextFile(filename+"3",ForWriting);
var r;
var cdatalevel=1;

function ProcessNextLine(f, f2, level,skipline) 
{
	var cytatmode=false;
	var sl=skipline;
	var first=true;
	while (1) {
		if (f.AtEndOfStream) {
			if (!first) {
				if (level==2) f2.WriteLine ("</ustep>");
				if (level==3) f2.WriteLine ("</punkt>");
				if (level==4) f2.WriteLine ("</litera>");
				if (level==5) f2.WriteLine ("</tiret>");

			}
			break;
		}
		if (sl) {
			sl=false;
		} else {
			r = f.ReadLine().LTrim().RTrim();
		}
		if (r=="") {
			continue;
		}
		if (r=="(Watermark)") {
			continue;
		}
		if (r=="") {
			continue;
		}
//		if (r.indexOf("Dziennik Ustaw Nr 30 —")!=-1 && r.indexOf(" — Poz. 151")!=-1) {
//		if (r.indexOf("Dziennik Ustaw Nr 92 —")!=-1 && r.indexOf(" — Poz. 530")!=-1) {
//		if (r.indexOf("Dziennik Ustaw Nr 2 –")!=-1 && r.indexOf(" – Poz. 113")!=-1) {
//			continue;
//		}
		if (r.indexOf("Dziennik Ustaw Nr 98 – ")!=-1) {
			r = f.ReadLine().LTrim().RTrim();
			continue;
		}

		if (level==-1) {

			if (r.indexOf("Rozdzia³ ")==0) {
				f2.WriteLine ("<rozdzial nr=\""+r.substr(9)+"\">");
				ProcessNextLine(f,f2,1,false);
				sl=true;
				f2.WriteLine ("</rozdzial>");
				continue;
			} else if (r.indexOf("Art. ")==0) {
				ProcessNextLine(f,f2,1,true);
				sl=true;
				continue;

			} else if (r.search(/[a-z0-9]+\. /)==0  && r.search(/[0-9]/)==0) {
				ProcessNextLine(f,f2,2,true);
				sl=true;
				continue;
			} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)==0) {
				ProcessNextLine(f,f2,3,true);
				sl=true;
				continue;
			} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[a-z]/)==0) {
				ProcessNextLine(f,f2,4,true);
				sl=true;
				continue;
			} else if (r.indexOf("— ")==0) {
				ProcessNextLine(f,f2,5,true);
				sl=true;
				continue;
			} else {
				f2.WriteLine (r);
			}
		} else if (level==0) {
			if (r.indexOf("Rozdzia³ ")==0) {
				f2.WriteLine ("<rozdzial nr=\""+r.substr(9)+"\">");
				ProcessNextLine(f,f2,1,false);
				sl=true;
				f2.WriteLine ("</rozdzial>");
				continue;
			} else if (r.indexOf("DZIA£ ")==0) {
				f2.WriteLine ("<dzial nr=\""+r.substr(9)+"\">");
				ProcessNextLine(f,f2,6,false);
				sl=true;
				f2.WriteLine ("</dzial>");
				continue;
			} else if (r.indexOf("Oddzia³ ")==0) {
				f2.WriteLine ("<oddzial nr=\""+r.substr(9)+"\">");
				ProcessNextLine(f,f2,7,false);
				sl=true;
				f2.WriteLine ("</oddzial>");
				continue;
			} else if (r.indexOf("Art. ")==0) {
				ProcessNextLine(f,f2,1,true);
				sl=true;
				continue;
			} else {
				f2.WriteLine (r);
			}
		} else if (level==1) { //artyku³
			if (r.indexOf("Rozdzia³ ")==0) {
				break;
			} else {
				if (r.indexOf("Art. ")==0) {
					w = r.substr(5);					
					f2.WriteLine ("<artykul nr=\""+w.substring(0,w.indexOf(" ")-1)+"\">");
					if (w.substr(w.indexOf(" ")+1).indexOf("1. ")==0) {
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,2,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					} else {
//						f2.WriteLine (w.substr(w.indexOf(" ")+1));
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,3,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					}
				} else {
					f2.WriteLine (r);
				}
			}
		} else if (level==2) { //ustep 1.
			if (r.indexOf("Art. ")==0 || r.indexOf("Rozdzia³ ")==0) {
				if (!first) f2.WriteLine("</ustep>");
				break;
			} else {
				if (r.search(/[a-z0-9]+\. /)==0  && r.search(/[0-9]/)==0) {
					if (!first) f2.WriteLine("</ustep>");
					f2.WriteLine ("<ustep nr=\""+r.substring(0,r.indexOf(" ")-1)+"\">");
					f2.WriteLine (r.substring(r.indexOf(" ")+1));
					first=false;
				} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)==0) {
					ProcessNextLine(f,f2,3,true);
					sl=true;
					continue;
				} else {
					f2.WriteLine (r);
				}
			}
		} else if (level==3) { //pkt 1)
			if (cytatmode) {
				f3.WriteLine (r);
				if (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2) {

					f3.close();

					f3 = fso.OpenTextFile(filename+"temp"+cdatalevel, ForReading);
					cdatalevel++;
					f2.WriteLine("<![CDATA[");
					ProcessNextLine(f3,f2,-1,false);
					f2.WriteLine("]]>");
					cdatalevel--;

					f3.close();

					cytatmode=false;
				}
				continue;
			}
			if (r.search(/[a-z0-9]+\. /)==0 && r.search(/[0-9]/)==0) {
				if (!first) f2.WriteLine("</punkt>");
				break;
			} else if (r.indexOf("Art. ")==0 || r.indexOf("Rozdzia³ ")==0) {
				if (!first) f2.WriteLine("</punkt>");
				break;
			} else {
				if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)==0) {
					if (!first) f2.WriteLine("</punkt>");
					f2.WriteLine ("<punkt nr=\""+r.substring(0,r.indexOf(" ")-1)+"\">");
					f2.WriteLine (r.substring(r.indexOf(" ")+1));
					first=false;
				} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[a-z]/)==0) {
					ProcessNextLine(f,f2,4,true);
					sl=true;
					continue;
				} else {
					if (r.indexOf("„")==0) {
						f3 = fso.CreateTextFile(filename+"temp"+cdatalevel,ForWriting);
						cytatmode=true;
					}
					if (cytatmode && (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2)) {
						cytatmode=false;
						f3.close();
					}
					if (cytatmode) {
						f3.WriteLine (r.substring(1));
					} else {
						f2.WriteLine (r);
					}
				}
			}
		} else if (level==4) { //lit a)
			if (cytatmode) {
				f3.WriteLine (r);
				if (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2) {
					f3.close();

					f3 = fso.OpenTextFile(filename+"temp"+cdatalevel, ForReading);
					cdatalevel++;
					f2.WriteLine("<![CDATA[");
					ProcessNextLine(f3,f2,-1,false);
					f2.WriteLine("]]>");
					cdatalevel--;

					f3.close();

					cytatmode=false;
				}
				continue;
			}
			if (r.search(/[a-z0-9]+\. /)==0 && r.search(/[0-9]/)==0) {
				if (!first) f2.WriteLine("</litera>");
				break;
			} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)==0) {
				if (!first) f2.WriteLine("</litera>");
				break;
			} else if (r.indexOf("Art. ")==0 || r.indexOf("Rozdzia³ ")==0) {
				if (!first) f2.WriteLine("</litera>");
				break;
			} else {
				if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)!=0) {
					if (!first) f2.WriteLine("</litera>");
					f2.WriteLine ("<litera nr=\""+r.substring(0,r.indexOf(" ")-1)+"\">");
					f2.WriteLine (r.substring(r.indexOf(" ")+1));
					first=false;
				} else if (r.indexOf("— ")==0) {
					ProcessNextLine(f,f2,5,true);
					sl=true;
					continue;
				} else {
					if (r.indexOf("„")==0) {
						f3 = fso.CreateTextFile(filename+"temp"+cdatalevel,ForWriting);
						cytatmode=true;
					}
					if (cytatmode && (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2)) {
						cytatmode=false;
						f3.close();
					}
					if (cytatmode) {
						f3.WriteLine (r.substring(1));
					} else {
						f2.WriteLine (r);
					}
				}
			}
		} else if (level==5) { //tiret
			if (cytatmode) {
				f3.WriteLine (r);
				if (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2) {

					f3.close();

					f3 = fso.OpenTextFile(filename+"temp"+cdatalevel, ForReading);
					cdatalevel++;
					f2.WriteLine("<![CDATA[");
					ProcessNextLine(f3,f2,-1,false);
					f2.WriteLine("]]>");
					cdatalevel--;

					f3.close();

					cytatmode=false;
				}
				continue;
			}
			if (r.search(/[a-z0-9]+\. /)==0 && r.search(/[0-9]/)==0) {
				if (!first) f2.WriteLine("</tiret>");
				break;
			} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[0-9]/)==0) {
				if (!first) f2.WriteLine("</tiret>");
				break;
			} else if (r.search(/[a-z0-9]+\) /)==0 && r.search(/[a-z]/)==0) {
				if (!first) f2.WriteLine("</tiret>");
				break;
			} else if (r.indexOf("Art. ")==0 || r.indexOf("Rozdzia³ ")==0) {
				if (!first) f2.WriteLine("</tiret>");
				break;
			} else {
				if (r.indexOf("— ")==0) {
					if (!first) f2.WriteLine("</tiret>");
					f2.WriteLine ("<tiret>");
					f2.WriteLine (r.substring(r.indexOf(" "+1)));
					first=false;
				} else {
					if (r.indexOf("„")==0) {
						f3 = fso.CreateTextFile(filename+"temp"+cdatalevel,ForWriting);
						cytatmode=true;
					}
					if (cytatmode && (r.lastIndexOf("”,")==r.length-2 || r.lastIndexOf("”;")==r.length-2 || r.lastIndexOf("”.")==r.length-2)) {
						cytatmode=false;
						f3.close();
					}
					if (cytatmode) {
						f3.WriteLine (r.substring(1));
					} else {
						f2.WriteLine (r);
					}
				}
			}
		} else if (level==6) { //dzial
			if (r.indexOf("Rozdzia³ ")==0) {
				break;
			} else {
				if (r.indexOf("Art. ")==0) {
					w = r.substr(5);					
					f2.WriteLine ("<artykul nr=\""+w.substring(0,w.indexOf(" ")-1)+"\">");
					if (w.substr(w.indexOf(" ")+1).indexOf("1. ")==0) {
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,2,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					} else {
//						f2.WriteLine (w.substr(w.indexOf(" ")+1));
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,3,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					}
				} else if (r.indexOf("Rozdzia³ ")==0) {
					f2.WriteLine ("<rozdzial nr=\""+r.substr(9)+"\">");
					ProcessNextLine(f,f2,1,false);
					sl=true;
					f2.WriteLine ("</rozdzial>");
					continue;
				} else {
					f2.WriteLine (r);
				}
			}
		} else if (level==7) { //oddzial
			if (r.indexOf("Rozdzia³ ")==0) {
				break;
			} else {
				if (r.indexOf("Art. ")==0) {
					w = r.substr(5);					
					f2.WriteLine ("<artykul nr=\""+w.substring(0,w.indexOf(" ")-1)+"\">");
					if (w.substr(w.indexOf(" ")+1).indexOf("1. ")==0) {
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,2,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					} else {
//						f2.WriteLine (w.substr(w.indexOf(" ")+1));
						r=w.substr(w.indexOf(" ")+1);
						ProcessNextLine(f,f2,3,true);
						sl=true;
						f2.WriteLine ("</artykul>");
						continue;
					}
				} else if (r.indexOf("Rozdzia³ ")==0) {
					f2.WriteLine ("<rozdzial nr=\""+r.substr(9)+"\">");
					ProcessNextLine(f,f2,1,false);
					sl=true;
					f2.WriteLine ("</rozdzial>");
					continue;
				} else {
					f2.WriteLine (r);
				}
			}
		}

	}
}

f2.WriteLine ("<?xml version=\"1.0\" encoding=\"windows-1250\" ?>");

f2.WriteLine ("<ustawa>");			
ProcessNextLine(f,f2,0,false);
f2.WriteLine ("</ustawa>");

f.Close();
f2.close();

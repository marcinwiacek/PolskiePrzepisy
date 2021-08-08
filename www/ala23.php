<?php

function getAttribute($node,$name) {
	foreach ($node->attributes as $attrName => $attrValue) {
		if ($attrName==$name) return $attrValue->value;
	}
	return "";
}

function getText2($node,$ztagami) {
	$retval="";
	foreach ($node->childNodes as $child) {
        	if ($child->nodeType==XML_ELEMENT_NODE) {
			if ($child->nodeName=="przypis" && $ztagami) {
				$retval=$retval."<a href=#przypis".getAttribute($child,"nr").">".getAttribute($child,"nr").") </a> ";
			}
		} else {
			$retval=$retval.$child->textContent;
		}
	}
	return $retval;
}

function pokaz($node,$first,$firstlevel) {
	global $poczatek,$koniec,$opis;

	if ($node->nodeType!=XML_ELEMENT_NODE) {
		return;
	}

	echo $koniec;
	$koniec="";

	if ($node->nodeName=="akt-prawny") {
		$opis="Wersja ujednolicona automatycznie na podstawie<br><a href=".getAttribute($node,"numer-aktu").">".getAttribute($node,"opis-aktu")."</a> (ogłoszony ".getAttribute($node,"data-ogloszenia").")<br>".$opis;
		echo "<center>".$opis."</center><hr>";
	}
	
	if ($node->nodeName=="ustawa") {
		echo "<center>USTAWA</center><p><center>z dnia ".getAttribute($node,"data-wydania")."</center>";
	}
	if ($node->nodeName=="rozdzial") {
		echo "<p><center>Rozdział ".getAttribute($node,"nr")."</center>";
	}
	if ($node->nodeName=="tytul") {
		echo "<p><center><b>".getText2($node,true)."</b></center>";
		return;
	}
	if ($node->nodeName=="tekst" || $node->nodeName=="tekstem") {
		$poczatek="";
		return;
	}

	if ($node->nodeName=="artykul") {
		echo "<p><b>".$poczatek."Art. ".getAttribute($node,"nr")."</b> ";
		$poczatek="";
		if (getAttribute($node,"uchylony")=="tak") {
			echo " (uchylony)";
			return;
		}
	}
	if ($node->nodeName=="ustep") {
		if (!$first) {
			echo "<p>";
		}
		echo $poczatek.getAttribute($node,"nr").". ";
		$poczatek="";
		if (getAttribute($node,"uchylony")=="tak") {
			echo " (uchylony)";
			return;
		}
	}
	if ($node->nodeName=="punkt") {
		if (!$firstlevel) {
			echo "<div style=\"margin-left:30px\">";
		}
		echo "<p>".$poczatek.getAttribute($node,"nr").") ";
		$poczatek="";
		if (getAttribute($node,"uchylony")=="tak") {
			echo " (uchylony)";
			if (!$wcieciepoczatek) {
				$koniec=$koniec."</div>";
			}
			return;
		}
	}
	if ($node->nodeName=="litera") {
		if (!$firstlevel) {
			echo "<div style=\"margin-left:30px\">";
		}
		echo "<p>".$poczatek.getAttribute($node,"nr").") ";
		$poczatek="";
		if (getAttribute($node,"uchylony")=="tak") {
			echo " (uchylony)";
			if (!$wcieciepoczatek) {
				$koniec=$koniec."</div>";
			}
			return;
		}
	}
	if ($node->nodeName=="tiret") {
		if (!$firstlevel) {
			echo "<div style=\"margin-left:30px\">";
		}
		echo "<p>— ".$poczatek;
		$poczatek="";
		if (getAttribute($node,"uchylony")=="tak") {
			echo " (uchylony)";
			if (!$wcieciepoczatek) {
				$koniec=$koniec."</div>";
			}
			return;
		}
	}
	if ($node->nodeName=="wciecie") {
		if (!$firstlevel) {
			echo "<div style=\"margin-left:30px\">";
		}
		echo "<p>— ".$poczatek;
		$poczatek="";
	}
	if ($node->nodeName=="tresc-przypisu") {
		if (getAttribute($node,"nr")=="1") {
			echo "<hr>";
		}
		echo "<p><a name=przypis".getAttribute($node,"nr").">".getAttribute($node,"nr").") ";
	}
	if ($node->nodeName=="podpisy") {
		echo "<p>".getText2($node,true);
		return;
	}
	if ($node->nodeName=="zastap") {
		echo "<p>";
		echo "<div style=\"margin-left:30px\">";
		$poczatek="„";
	}
	if ($node->nodeName=="dodaj") {
		echo "<p>";
		echo "<div style=\"margin-left:30px\">";
		$poczatek="„";
	}
	if ($node->nodeName=="zmien-tytul") {
		$poczatek=" „";
	}

	$flevel=false;
	if ($node->nodeName=="element" || $node->nodeName=="elementem") {
		$flevel=true;
	}
	$f=true;
        foreach ($node->childNodes as $child) {
		if ($child->nodeType==XML_ELEMENT_NODE) {
			pokaz($child,$f,$flevel);
		} else if ($child->nodeType == XML_TEXT_NODE) {
			echo $poczatek;
			echo preg_replace("/[\r\n]+/"," ",trim($child->textContent));
			$poczatek="";
			$flevel=false;
		}
		$f=false;
	}

	if ($node->nodeName=="zastap" || $node->nodeName=="dodaj" || $node->nodeName=="zmien-tytul") {
		echo "”";
	}

	if ($node->nodeName=="punkt" || $node->nodeName=="litera" || 
	    $node->nodeName=="tiret" || $node->nodeName=="wciecie" ||
	    $node->nodeName=="dodaj" || $node->nodeName=="zastap") {
		if (!$firstlevel) {
			$koniec=$koniec."</div>";
		}
	}	
}

function aktualizuj($node,$datajednolita,$akt) {
	global $dom,$opis,$tenopis;
	if ($node->nodeType!=XML_ELEMENT_NODE) {
		return;
	}
	if ($node->nodeName=="akt-prawny") {
		if (strcmp($datajednolita,getAttribute($node,"data-ogloszenia"))<0) return;
		if ($opis!="") $tenopis=$tenopis."<br>";
		$tenopis=$tenopis."<a href=".getAttribute($node,"numer-aktu").">".getAttribute($node,"opis-aktu")."</a> (ogłoszony ".getAttribute($node,"data-ogloszenia").")";
	}
	if ($node->nodeName=="zastap") {
		if (getAttribute($node,"w-ustawie")!=$akt) return;

		if ($tenopis!="") {
			$opis=$opis.$tenopis;
			$tenopis="";
		}

		$welemencie=null;
		$element=null;
		$elementy=null;
		$elementem=null;
		$tekst=null;
		$tekstem=null;
		foreach ($node->childNodes as $child) {
			if ($child->nodeType==XML_ELEMENT_NODE) {
				if ($child->nodeName=="element") {
					$element=$child;
				}
				if ($child->nodeName=="elementy") {
					$elementy=$child;
				}
				if ($child->nodeName=="elementem") {
					$elementem=$child;
				}
				if ($child->nodeName=="tekst") {
					$tekst=$child;
				}
				if ($child->nodeName=="tekstem") {
					$tekstem=$child;
				}
				if ($child->nodeName=="w-elemencie") {
					$welemencie=$child;
				}
			}
		}
		if ($element!=null && $elementem!=null) {
			$xpath = new DOMXPath($dom);
			$entries = $xpath->query(getAttribute($element,"id"));
			$num=0;
			foreach ($entries as $entry) {
				$num++;
			}
			if ($num!=1) {
				echo "Zastap - różna od 1 ilość elementów do zastapienia dla id ".getAttribute($element,"id");
				return;
			}
			$oldentry = $entries->item(0);
			$newentry = $dom->importNode($elementem->childNodes->item(0), true);
			$oldentry->parentNode->replaceChild($newentry, $oldentry); 
		} else if ($welemencie!=null && $tekst!=null && $tekstem!=null) {
			$xpath = new DOMXPath($dom);
			$entries = $xpath->query(getAttribute($welemencie,"id"));
			$num=0;
			foreach ($entries as $entry) {
				$num++;
			}
			if ($num!=1) {
				echo "Zastap - różna od 1 ilość elementów do zastapienia dla id ".getAttribute($element,"id");
				return;
			}
			$znalazl=false;
			$count=0;
			foreach ($entries->item(0)->childNodes as $child) {
				if ($child->nodeType!=XML_TEXT_NODE) {
					continue;
				}
				$s=preg_replace("/[\r\n]+/"," ",trim($child->textContent));
				$s2=str_replace ($tekst->childNodes->item(0)->textContent,$tekstem->childNodes->item(0)->textContent, $s,$count);
				if ($count>0) {
					$child->parentNode->replaceChild(new DOMText($s2), $child); 

					$znalazl=true;
					break;
				}
			}
			if (!$znalazl) {
				echo "Zastap - problem z zastąpieniem tekstu dla id ".getAttribute($welemencie,"id");
				return;
			}
			
		} else if ($elementy!=null && $elementem!=null) {
			$xpath = new DOMXPath($dom);
			$od = $xpath->query(getAttribute($elementy,"od"));
			$num=0;
			foreach ($od as $entry) {
				$num++;
			}
			if ($num!=1) {
				echo "Zastap - różna od 1 ilość elementów dla id ".getAttribute($elementy,"od");
				return;
			}
			$do = $xpath->query(getAttribute($elementy,"do"));
			$num=0;
			foreach ($do as $entry) {
				$num++;
			}
			if ($num!=1) {
				echo "Zastap - różna od 1 ilość elementów dla id ".getAttribute($elementy,"do");
				return;
			}
			if ($od->item(0)->parentNode!=$do->item(0)->parentNode) {
				echo "Zastap - różny parent";
				return;
			}
			$num=0;
			foreach ($od->item(0)->parentNode->childNodes as $child) {
				if ($child->nodeType!=XML_ELEMENT_NODE) {
					continue;
				}
				if ($child===$od->item(0)) {
					$num=0;
				}
				$num++;
				if ($child===$do->item(0)) {
					break;
				}
			}

			$num2=0;
			foreach ($elementem->childNodes as $child) {
				$num2++;
			}
			if ($num!=$num2) {
				echo "Zastap - różna ilość elementów źródłowych i docelowych";
				return;
			}

			$num=0;
			foreach ($od->item(0)->parentNode->childNodes as $child) {
				$num++;
				if ($child->nodeType!=XML_ELEMENT_NODE) {
					continue;
				}
				if ($child===$od->item(0)) {
					break;
				}
			}

			$i=$num-1;
			$num=0;
			while ($num!=$num2) {
				if ($od->item(0)->parentNode->childNodes->item($i)->nodeType!=XML_ELEMENT_NODE) {
					$i++;
					continue;
				}
				$newentry = $dom->importNode($elementem->childNodes->item($num), true);
				$od->item(0)->parentNode->replaceChild($newentry,$od->item(0)->parentNode->childNodes->item($i));

				$xpath = new DOMXPath($dom);
				$od = $xpath->query(getAttribute($elementy,"od"));

				$i++;
				$num++;
			}
		} else {
			echo "Nie znaleziono wartosci dla zastap - ";
			if ($element==null) echo "element jest pusty";
			if ($elementy==null) echo "elementy jest pusty";
			if ($elementem==null) echo "elementem jest pusty";
			echo "<p>";
		}
		return;
	}
	if ($node->nodeName=="uchyl") {
		if (getAttribute($node,"w-ustawie")!=$akt) return;

		if ($tenopis!="") {
			$opis=$opis.$tenopis;
			$tenopis="";
		}

		$element=null;
		foreach ($node->childNodes as $child) {
			if ($child->nodeType==XML_ELEMENT_NODE) {
				if ($child->nodeName=="element") {
					$element=$child;
				}
			}
		}
		if ($element!=null) {
			$xpath = new DOMXPath($dom);
			$entries = $xpath->query(getAttribute($element,"id"));
			$num=0;
			foreach ($entries as $entry) {
				$num++;
			}
			if ($num!=1) {
				echo "Uchyl - różna od 1 ilość elementów do zastapienia dla id ".getAttribute($element,"id");
				return;
			}
			$oldentry = $entries->item(0);
			$oldentry->setAttribute("uchylony","tak");
			while ($oldentry->childNodes->length) {
				$oldentry->removeChild($oldentry->firstChild);
			}
		} else {
			echo "Nie znaleziono wartosci dla uchyl - ";
			if ($element==null) echo "element jest pusty";
			echo "<p>";
		}
		return;
	}
	foreach ($node->childNodes as $child) {
		if ($child->nodeType==XML_ELEMENT_NODE) {					
			aktualizuj($child,$datajednolita,$akt);
		}
	}	
}

function szukaj($node) {
	global $wyjdz,$nazwa;

	if ($node->nodeType!=XML_ELEMENT_NODE) {
		return;
	}
	if ($node->nodeName=="akt-prawny") {
		$nazwa=$nazwa.getAttribute($node,"opis-aktu");
	}

	if ($node->nodeName=="ustawa") {
		$nazwa=$nazwa." (Ustawa z dnia ".getAttribute($node,"data-wydania");
	}
	if ($node->nodeName=="tytul") {
		$nazwa=$nazwa." ".getText2($node,false).")";
		$wyjdz=true;
		return;
	}

	foreach ($node->childNodes as $child) {
		if ($wyjdz) return;
		if ($child->nodeType==XML_ELEMENT_NODE) {
			szukaj($child);
		}
	}	
}

function szukajid($node) {
	global $wyjdz,$identyfikator;

	if ($node->nodeType!=XML_ELEMENT_NODE) {
		return;
	}
	if ($node->nodeName=="akt-prawny") {
		$identyfikator=getAttribute($node,"numer-aktu");
		$wyjdz=true;
	}
        foreach ($node->childNodes as $child) {
		if ($wyjdz) return;
		if ($child->nodeType==XML_ELEMENT_NODE) {
			szukajid($child);
		}
	}	
}
    
echo "<html><head>";
echo "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">";
echo "</head><body>";

echo "<table><form method=post action=ala23.php>";
echo "<tr><td>Akt prawny:</td><td><select name=akt>";
$files = scandir('.');
foreach ($files as $file) {
	if (!strstr($file,".xml")) {
		continue;
	}
	$dom = new DomDocument();
	$dom->preserveWhiteSpace = false;
	$dom->substituteEntities = true;
	if (!$dom->load($file)) {
		echo "Błąd ładowania pliku";
	}
	$wyjdz=false;
	$nazwa="";
	szukaj($dom->documentElement);
	echo "<option value=\"".$file."\"";
	if (isset($_POST['akt']) && $_POST['akt']==$file) {
		echo " selected";
	}
	echo">".$nazwa."</option>";
	unset($dom);
}
echo "</select></td></tr>";
echo "<tr><td colspan=2>";
echo "<input type=\"checkbox\" name=\"jednolita\" value=\"1\" ";
if (isset($_POST['jednolita'])) echo "checked";
echo "/> Wersja jednolita na dzień <input name='dt' ";
if (isset($_POST['dt'])) {
	echo " value='".$_POST['dt']."' ";
} else {
	echo " value='20130803' ";
}
echo "</td></tr>";
echo "<tr><td colspan=2><input type=submit></td></tr>";
echo "</form></table>";

if (isset($_POST['akt'])) {
	echo "<hr>";

	$identyfikator="";
	$opis="";

	$dom = new DomDocument();
	$dom->preserveWhiteSpace = false;
	$dom->substituteEntities = true;
	$dom->load($_POST['akt']);
	szukajid($dom->documentElement);

	if (isset($_POST['jednolita']) && isset($_POST['dt'])) {
		$files = scandir('.');
		foreach ($files as $file) {
			if (strstr($file,".xml") && $file!=$_POST['akt']) {
				$dom2 = new DomDocument();
				$dom2->preserveWhiteSpace = false;
				$dom2->substituteEntities = true;
				$dom2->load($file);
				$tenopis="";
				aktualizuj($dom2->documentElement,$_POST['dt'],$identyfikator);
				unset($dom2);
			}
		}
	}

	$poczatek="";
	$koniec="";

	pokaz($dom->documentElement,true,true);
}

echo "</body></html>";

?>

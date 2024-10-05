USE ospedale;
-- OP4: Elenco dei medici per numero di interventi eseguiti
SELECT nome, cognome, interventi_eseguiti, reparto
FROM Medico
ORDER BY interventi_eseguiti DESC;

-- OP5: Elenco dei pazienti che hanno prenotato un trattamento nell’ultimo mese ordinati per numero di trattamenti sostenuti (ascendenti)
SELECT p.codice_fiscale, p.nome, p.cognome, p.trattamenti_sostenuti, MAX(t.data_prenotazione) AS ultima_prenotazione
FROM Paziente p
JOIN Trattamento t ON p.codice_fiscale = t.paziente
WHERE t.data_prenotazione >= DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)
GROUP BY p.codice_fiscale, p.nome, p.cognome, p.trattamenti_sostenuti
ORDER BY trattamenti_sostenuti, ultima_prenotazione;

-- OP6: Elenco dei trattamenti eseguiti da un medico specifico in una data fascia temporale
SELECT t.codice, t.nome, t.data_concordata, t.paziente
FROM Trattamento t
JOIN Svolgimento s ON t.codice = s.trattamento
WHERE s.medico = ?
AND t.data_concordata BETWEEN ? AND ?;

-- OP7: Durata media delle operazioni chirurgiche per reparto 
SELECT r.specializzazione, SEC_TO_TIME(AVG(TIME_TO_SEC(t.durata))) AS durata_media
FROM Reparto r
JOIN Medico m ON r.specializzazione = m.reparto
JOIN Svolgimento s ON s.medico = m.id
JOIN Trattamento t ON s.trattamento = t.codice
WHERE t.tipo = 'operazione chirurgica'
GROUP BY r.specializzazione;

-- OP8: Elenco dei trattamenti in attesa con la data concordata odierna
SELECT t.codice, t.nome, t.data_concordata, t.ora_concordata, t.tipo, t.stato, t.paziente
FROM Trattamento t
WHERE t.data_concordata = CURDATE() AND t.stato = 'in attesa';

-- OP9: Elenco dei pazienti che hanno effettuato più di un trattamento nello stesso giorno
SELECT p.codice_fiscale, p.nome, p.cognome, t.data_concordata, COUNT(t.codice) AS num_trattamenti_eseguiti
FROM Paziente p
JOIN Trattamento t ON p.codice_fiscale = t.paziente
WHERE t.stato = 'eseguito'
GROUP BY t.paziente, t.data_concordata
HAVING COUNT(t.codice) > 1;

-- OP10: Elenco del numero di utilizzi delle apparecchiature diagnostiche ordinate per data di acquisto
SELECT ad.codice_alfa, ad.nome AS nome_apparecchiatura, ad.data_di_acquisto, SUM(u.utilizzi) AS num_utilizzi
FROM Apparecchiatura_diagnostica ad
JOIN Utilizzo u ON ad.codice_alfa = u.apparecchiatura_diagnostica
GROUP BY ad.codice_alfa, ad.nome, ad.data_di_acquisto
ORDER BY ad.data_di_acquisto ASC;

-- OP11: Archiviazione dei trattamenti non eseguiti
UPDATE Trattamento
SET stato = 'non eseguito'
WHERE data_concordata <= CURDATE() AND stato = 'in attesa';

-- OP12: Elenco degli infermieri per reparto ordinati per numero di interventi assistiti
SELECT i.reparto, i.nome, i.cognome, COUNT(a.trattamento) AS interventi_assistiti
FROM Infermiere i
JOIN Assistenza a ON i.id = a.infermiere
GROUP BY i.reparto, i.nome, i.cognome
ORDER BY reparto, interventi_assistiti DESC;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JOptionPane;

public class Tester {
    public static void main(String[] args) {

		int continua = JOptionPane.YES_OPTION;
        
		while(continua == JOptionPane.YES_OPTION) {

			DbManager.initCon();
			DbManager.beginTransaction();

			int scelta = Integer.parseInt(JOptionPane.showInputDialog("Operazione da eseguire (0 per visualizzarle)"));

			try {
				switch(scelta) {
					case 0: {
						stampaOperazioni();
						break;
					}
					case 1: {
						registrazionePaziente();
						break;
					}
					case 2: {
						prenotazioneTrattamento();
						break;
					}
					case 3: {
						svolgimentoTrattamento();
						break;
					}
					case 4: {
						JOptionPane.showMessageDialog(null, DbManager.queryMediciPerNumeroOp());
						break;
					}
					case 5: {
						JOptionPane.showMessageDialog(null, DbManager.queryPazientiPrenotatiPerNumeroTrattamenti());
						break;
					}
					case 6 : {
						stampaMedicoPerDataSpecifica();
						break;
					}
					case 7 : {
						JOptionPane.showMessageDialog(null, DbManager.queryRepartiConDurataMedia());
						break;
					}
					case 8 : {
						JOptionPane.showMessageDialog(null, DbManager.queryTrattamentiDaSvolgereOggi());
						break;
					}
					case 9 : {
						JOptionPane.showMessageDialog(null, DbManager.queryPazientiConPiùTrattGiornalieri());
						break;
					}
					case 10: {
						JOptionPane.showMessageDialog(null, DbManager.queryApparecchiaturePerDataAcquisto());
						break;
					}
					case 11: {
						DbManager.updateTrattamenti();
						JOptionPane.showMessageDialog(null, "Operazione effettuata con successo," + 
						" i trattamenti non svolti sono ora contrassegnati con lo stato: 'non eseguito'");
						break;
					}
					case 12: {
						JOptionPane.showMessageDialog(null, DbManager.queryInfermieriPerInterventi());
						break;
					}
					case 13: {
						registrazionePersonale();
						break;
					}
					default : break;
				}
			} catch (Throwable t) {
				JOptionPane.showMessageDialog(null, t.getMessage(), null, JOptionPane.ERROR_MESSAGE);
				DbManager.rollbackTransaction();
				DbManager.closeCon();
				continua = JOptionPane.showConfirmDialog(null, "Eseguire un'altra operazione?", null, JOptionPane.YES_NO_OPTION);
				continue;
			}

			DbManager.commitTransaction();
			DbManager.closeCon();

			continua = JOptionPane.showConfirmDialog(null, "Eseguire un'altra operazione?", null, JOptionPane.YES_NO_OPTION);
		}
    }

	private static void stampaOperazioni() {

		JOptionPane.showMessageDialog(null, 
		"1: Registrazione di un nuovo paziente " +
		"\n2: Registrazione di un trattamento prenotato" +
		"\n3: Svolgimento di un trattamento (generico)" +
		"\n4: Stampa elenco dei medici per numero di interventi eseguiti" +
		"\n5: Stampa elenco dei pazienti che hanno prenotato un trattamento nell’ultimo mese ordinati per numero di trattamenti sostenuti" +
		"\n6: Stampa dei trattamenti eseguiti da un medico specifico in una data fascia temporale" +
		"\n7: Stampa durata media delle operazioni chirurgiche per reparto" +
		"\n8: Stampa elenco dei trattamenti in attesa con la data concordata odierna" +
		"\n9: Stampa elenco dei pazienti che hanno effettuato più di un trattamento nello stesso giorno" +
		"\n10: Stampa del numero di utilizzi delle apparecchiature diagnostiche ordinate per data di acquisto" +
		"\n11: Archiviazione dei trattamenti non eseguiti" +
		"\n12: Stampa elenco degli infermieri per reparto ordinati per numero di interventi assistiti" +
		"\n13: Registrazione di un membro del personale ospedaliero");
	}

	private static void registrazionePaziente() throws SQLException {
		
		//Input paziente
		String cf = JOptionPane.showInputDialog("Inserire il codice fiscale del paziente");
		String nome = JOptionPane.showInputDialog("Inserire il nome del paziente");
		String cognome = JOptionPane.showInputDialog("Inserire il cognome del paziente");
		LocalDate dataDiNascita = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data di nascita (formato: 'AAAA-MM-GG')"));
		String email = JOptionPane.showInputDialog("Inserire l'email del paziente per le comunicazioni (Formato: '_@_._')");

		DbManager.inserisciPaziente(cf, nome, cognome, dataDiNascita, email);
		
		JOptionPane.showMessageDialog(null, "Operazione effettuata con successo!");
	}

	private static void prenotazioneTrattamento() throws SQLException {
		
		//Input paziente
		String nomePaziente =JOptionPane.showInputDialog("Inserire il nome del paziente");
		String cognomePaziente = JOptionPane.showInputDialog("Inserire il cognome del paziente");
		LocalDate dataDiNascita = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data di nascita (formato: AAAA-MM-GG)"));
		//Controllo se il paziente è nel database e restituisco il CF
		String cfPaziente = Helper.trovaCfPaziente(nomePaziente, cognomePaziente, dataDiNascita);

		//Opzione di scelta tra esame medico e op. chirurgica
		String[] options = {"Esame medico", "Operazione chirurgica"};
		int tipo = JOptionPane.showOptionDialog(null, "Che tipo di trattamento si sta prenotando?",
			null, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		//Input trattamento
		String nome =JOptionPane.showInputDialog("Inserire il nome dello specifico trattamento");
		LocalDate data = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data del trattamento (formato: AAAA-MM-GG)"));
		LocalTime ora = LocalTime.parse(JOptionPane.showInputDialog("Inserire l'ora del trattamento (formato: HH:MM:SS)"));

		DbManager.inserisciTrattamento(tipo, cfPaziente, nome, data, ora);

		JOptionPane.showMessageDialog(null, "Operazione effettuata con successo!");
	}

	private static void svolgimentoTrattamento() throws SQLException {
		
		int codiceTrattamento;
		int conosciCodice = JOptionPane.showConfirmDialog(null, "Conosci il codice del trattamento?", null, JOptionPane.YES_NO_OPTION);

		if(conosciCodice == JOptionPane.YES_OPTION) {
			codiceTrattamento = Integer.parseInt(JOptionPane.showInputDialog("Inserire il codice del trattamento"));
		}
		else {
			//Input paziente
			String nomePaziente = JOptionPane.showInputDialog("Inserire il nome del paziente");
			String cognomePaziente = JOptionPane.showInputDialog("Inserire il cognome del paziente");
			LocalDate dataDiNascita = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data di nascita (formato: AAAA-MM-GG)"));
			//Controllo se il paziente è nel database e restituisco il CF
			String cfPaziente = Helper.trovaCfPaziente(nomePaziente, cognomePaziente, dataDiNascita);

			//Input trattamento
			String nome =JOptionPane.showInputDialog("Inserire il nome dello specifico trattamento");
			LocalDate data = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data del trattamento (formato: AAAA-MM-GG)"));
			//Controllo se il trattamento è nel database e restituisco il codice
			codiceTrattamento = Helper.trovaCodiceTrattamento(cfPaziente, nome, data);
			JOptionPane.showMessageDialog(null, "Trattamento trovato nel db, codice: " + codiceTrattamento);
		}

		String tipoTrattamento = Helper.trovaTipoTrattamento(codiceTrattamento);
		JOptionPane.showMessageDialog(null, "Il trattamento è del tipo " + tipoTrattamento);
		int scelta;

		do {
			//Input medico
			String nomeMedico =JOptionPane.showInputDialog("Inserire il nome del medico esecutore");
			String cognomeMedico =JOptionPane.showInputDialog("Inserire il cognome del medico esecutore");
			//Controllo se il medico è nel database e restituisco l'id
			int idMedico = Helper.trovaIdMedico(nomeMedico, cognomeMedico);
			JOptionPane.showMessageDialog(null, "Medico trovato nel db, codice: " + idMedico);
			
			DbManager.inserisciSvolgimento(codiceTrattamento, idMedico);
			//Trigger agiscono (!: Quando inserisco il secondo medico viene incrementato di 1 trattamenti sostenuti)
		
			scelta = JOptionPane.showConfirmDialog(null, "Ha partecipato un altro medico al trattamento?", null, JOptionPane.YES_NO_OPTION);
		} while (scelta == JOptionPane.YES_OPTION);

		scelta = JOptionPane.showConfirmDialog(null, "Sono state utilizzate delle apparecchiature diagnostiche?", null, JOptionPane.YES_NO_OPTION);
		
		while(scelta == JOptionPane.YES_OPTION) {
			//Input apparecchiatura diagnostica e utilizzi
			String nomeApparecchiatura =JOptionPane.showInputDialog("Inserire il nome dell'apparecchiatura diagnostica usata");
			String codiceApparecchiatura = Helper.trovaCodiceApparecchiatura(nomeApparecchiatura);
			
			DbManager.inserisciUtilizzo(codiceTrattamento, codiceApparecchiatura);

			int scelta2 = JOptionPane.showConfirmDialog(null, "E' stata utilizzata più volte?", null, JOptionPane.YES_NO_OPTION);

			if(scelta2 == JOptionPane.YES_OPTION) {
				int utilizzi = Integer.parseInt(JOptionPane.showInputDialog("Inserire quante volte è stata utilizzata durante il trattamento"));
				DbManager.updateUtilizzi(codiceTrattamento, codiceApparecchiatura, utilizzi);
			}
			
			scelta = JOptionPane.showConfirmDialog(null, "Sono state utilizzate altre apparecchiature diagnostiche?", null, JOptionPane.YES_NO_OPTION);
		}

		if(tipoTrattamento.equals("operazione")) {

			//Input durata
			LocalTime durata = LocalTime.parse(JOptionPane.showInputDialog("Inserire la durata dell'operazione appena svolta (formato HH:MM:00)"));
			DbManager.updateDurataOpChirurgica(codiceTrattamento, durata);

			scelta = JOptionPane.showConfirmDialog(null, "Hanno partecipato come assistenti degli infermieri?", null, JOptionPane.YES_NO_OPTION);
			
			while(scelta == JOptionPane.YES_OPTION) {
				//Input Infermiere
				int idInfermiere = Integer.parseInt(JOptionPane.showInputDialog("Inserire l'id dell'infermiere che ha assistito"));

				DbManager.inserisciAssistenza(codiceTrattamento, idInfermiere);
				
				scelta = JOptionPane.showConfirmDialog(null, "Hanno partecipato altri infermieri come assistenti?", null, JOptionPane.YES_NO_OPTION);
			}
		}

		JOptionPane.showMessageDialog(null, "Operazione effettuata con successo!");
	}

	private static void stampaMedicoPerDataSpecifica() throws SQLException {
		//Input medico
		String nomeMedico =JOptionPane.showInputDialog("Inserire il nome del medico");
		String cognomeMedico =JOptionPane.showInputDialog("Inserire il cognome del medico");
		//Controllo se il medico è nel database e restituisco l'id
		int idMedico = Helper.trovaIdMedico(nomeMedico, cognomeMedico);

		//Input range di date
		LocalDate dataInizio = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data iniziale del range (formato YYYY-MM-DD)"));
		LocalDate dataFine = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data finale del range (formato YYYY-MM-DD)"));

		JOptionPane.showMessageDialog(null, DbManager.queryTrattamentiFiltratoPerMedicoEDate(idMedico, dataInizio, dataFine));
	}

	private static void registrazionePersonale() throws SQLException {

		//Opzione di scelta tra P. amministrativo, infermiere e medico
		String[] options = {"Medico", "Infermiere", "Personale amministrativo"};
		int tipo = JOptionPane.showOptionDialog(null, "Che categoria di personale si sta registrando?",
			null, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		//Input dati comuni
		String nome = JOptionPane.showInputDialog("Inserire il nome del nuovo membro");
		String cognome = JOptionPane.showInputDialog("Inserire il cognome del nuovo membro");
		LocalDate dataDiNascita = LocalDate.parse(JOptionPane.showInputDialog("Inserire la data di nascita (formato: 'AAAA-MM-GG')"));
		
		if(tipo == 0) {
			String reparto = JOptionPane.showInputDialog("Inserire il reparto in cui opererà il nuovo medico registrato");
			DbManager.inserisciMedico(nome, cognome, dataDiNascita, reparto);
			JOptionPane.showMessageDialog(null, "ID assegnato al nuovo medico: " + Helper.ultimoIdMedico());
		}
		else if(tipo == 1) {
			String reparto = JOptionPane.showInputDialog("Inserire il reparto in cui opererà il nuovo infermiere registrato");
			DbManager.inserisciInfermiere(nome, cognome, dataDiNascita, reparto);
		}
		else if(tipo == 2) {
			String ruolo = JOptionPane.showInputDialog("Inserire il ruolo");
			DbManager.inserisciPAmministrativo(nome, cognome, dataDiNascita, ruolo);
		}

		JOptionPane.showMessageDialog(null, "Operazione effettuata con successo!");
	}
}

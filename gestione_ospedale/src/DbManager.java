import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JOptionPane;

public class DbManager {
	
	private static Connection con;
	
	//Gestione della connessione con il database

    public static void initCon() {
		try { 
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/ospedale"
				+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true"
				+ "&useLegacyDatetimeCode=false&serverTimezone=Europe/Rome";
			String username = "root";
			String pwd = "basi";
			con = DriverManager.getConnection(url,username,pwd);
		} 
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, "Connessione fallita", null, JOptionPane.ERROR_MESSAGE);
		}
		catch(ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Driver non caricato correttamente", null, JOptionPane.ERROR_MESSAGE);
		}
	}

    public static void closeCon() {
		try {
			con.close();
		}
		catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Chiusura della connessione fallita", null, JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void beginTransaction() {
		try {
			con.setAutoCommit(false);
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, "Errore nell'inizio della transazione", null, JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void commitTransaction() {
		try {
			con.commit();
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, "Errore nel completamento della transazione", null, JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void rollbackTransaction() {
		try {
			con.rollback();
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, "Errore nel rollbalck della transazione", null, JOptionPane.ERROR_MESSAGE);
		}
	}

	public static Connection getCon() {
		return con;
	}

    //Operazione 1: Registrazione di un nuovo paziente

	public static void inserisciPaziente(String cf, String nome, String cognome, LocalDate dataDiNascita, String email) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Paziente (codice_fiscale, nome, cognome, data_di_nascita, email) " +
			"VALUES (?, ?, ?, ?, ?)"
		);
		
		ps.setString(1, cf);
		ps.setString(2, nome);
		ps.setString(3, cognome);
		ps.setDate(4, Date.valueOf(dataDiNascita));
		ps.setString(5, email);

		ps.executeUpdate();
	}

	// Operazione 2: Registrazione di un trattamento prenotato

	public static void inserisciTrattamento(int tipo, String cfPaziente, String nome, LocalDate data, LocalTime ora) throws SQLException {
		PreparedStatement ps;
		if(tipo == 0) {

			ps = con.prepareStatement(
				"INSERT INTO Trattamento (nome, data_concordata, ora_concordata, paziente, data_prenotazione, tipo) " + 
				"VALUES (?, ?, ?, ?, ?, 'esame medico')"
			);
		} else if (tipo == 1) {

			ps = con.prepareStatement(
				"INSERT INTO Trattamento (nome, data_concordata, ora_concordata, paziente, data_prenotazione, tipo) " + 
				"VALUES (?, ?, ?, ?, ?, 'operazione chirurgica')"
			);
		} else {
			throw new RuntimeException("Errore nella specificazione del tipo di trattamento");
		}
		
		ps.setString(1, nome);
		ps.setDate(2, Date.valueOf(data));
		ps.setTime(3, Time.valueOf(ora));
		ps.setString(4, cfPaziente);
		ps.setDate(5, Date.valueOf(LocalDate.now()));
		
		ps.executeUpdate();
	}
	
	//Operazione 3: Svolgimento di trattamento

	public static void inserisciSvolgimento(int codiceTrattamento, int idMedico) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Svolgimento (medico, trattamento) " +
			"VALUES (?, ?)"
		);

		ps.setInt(1, idMedico);
		ps.setInt(2, codiceTrattamento);

		ps.executeUpdate();
		// Si azionano i triggers
	}

	public static void inserisciUtilizzo(int idTrattamento, String codiceApparecchiatura) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Utilizzo (apparecchiatura_diagnostica, trattamento) " +
			"VALUES (?, ?)"
		);

		ps.setString(1, codiceApparecchiatura);
		ps.setInt(2, idTrattamento);

		ps.executeUpdate();
	}

	public static void updateUtilizzi(int idTrattamento, String codiceApparecchiatura, int utilizzi) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"UPDATE Utilizzo " +
			"SET utilizzi = ? " + 
			"WHERE apparecchiatura_diagnostica = ? AND trattamento = ?"
		);

		ps.setInt(1, utilizzi);
		ps.setString(2, codiceApparecchiatura);
		ps.setInt(3, idTrattamento);

		ps.executeUpdate();
	}

	public static void updateDurataOpChirurgica(int codiceOperazione, LocalTime durata) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"UPDATE Trattamento " +
			"SET durata = ? " + 
			"WHERE tipo = 'operazione chirurgica' AND codice = ?"
		);

		ps.setTime(1, Time.valueOf(durata));
		ps.setInt(2, codiceOperazione);

		ps.executeUpdate();
	}

	public static void inserisciAssistenza(int codiceOperazione, int idInfermiere) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Assistenza (trattamento, infermiere) " +
			"VALUES (?, ?)"
		);

		ps.setInt(1, codiceOperazione);
		ps.setInt(2, idInfermiere);

		ps.executeUpdate();
	}

	//Operazione 4: Stampa elenco dei medici per numero di interventi eseguiti

	public static String queryMediciPerNumeroOp() throws SQLException {
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT nome, cognome, interventi_eseguiti, reparto\n" + //
			"FROM Medico\n" + //
			"ORDER BY interventi_eseguiti DESC;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Nome: " + rs.getString("nome") +
			"\nCognome: " + rs.getString("cognome") +
			"\nInterventi eseguiti: " + rs.getString("interventi_eseguiti") +
			"\nReparto: " + rs.getString("reparto") +
			"\n\n";
		}

		return result;
	}

	//Operazione 5: Stampa elenco dei pazienti che hanno prenotato un trattamento nell’ultimo mese ordinati per numero di trattamenti sostenuti

	public static String queryPazientiPrenotatiPerNumeroTrattamenti() throws SQLException {

		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT p.codice_fiscale, p.nome, p.cognome, p.trattamenti_sostenuti, MAX(t.data_prenotazione) AS ultima_prenotazione\n" + //
			"FROM Paziente p\n" + //
			"JOIN Trattamento t ON p.codice_fiscale = t.paziente\n" + //
			"WHERE t.data_prenotazione >= DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)\n" + //
			"GROUP BY p.codice_fiscale, p.nome, p.cognome, p.trattamenti_sostenuti\n" + //
			"ORDER BY trattamenti_sostenuti, ultima_prenotazione;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Codice fiscale paziente: " + rs.getString("codice_fiscale") +
			"\nNome: " + rs.getString("nome") +
			"\nCognome: " + rs.getString("cognome") +
			"\nTrattamenti sostenuti: " + rs.getString("trattamenti_sostenuti") +
			"\nUltima prenotazione: " + rs.getString("ultima_prenotazione") +
			"\n\n";
		}

		return result;
	}

	//Operazione 6: Stampa dei trattamenti eseguiti da un medico specifico in una data fascia temporale

	public static String queryTrattamentiFiltratoPerMedicoEDate(int idMedico, LocalDate dataInizio, LocalDate dataFine) throws SQLException {
		
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT t.codice, t.nome, t.data_concordata, t.paziente\n" + //
			"FROM Trattamento t\n" + //
			"JOIN Svolgimento s ON t.codice = s.trattamento\n" + //
			"WHERE s.medico = ?\n" + //
			"AND t.data_concordata BETWEEN ? AND ?;"
		);
		
		ps.setInt(1, idMedico);
		ps.setDate(2, Date.valueOf(dataInizio));
		ps.setDate(3, Date.valueOf(dataFine));

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Codice: " + rs.getInt("codice") +
			"\nNome: " + rs.getString("nome") +
			"\nData esecuzione: " + rs.getDate("data_concordata") +
			"\nPaziente: " + rs.getString("paziente") +
			"\n\n";
		}

		return result;
	}

	//Operazione 7: Stampa durata media delle operazioni chirurgiche per reparto:

	public static String queryRepartiConDurataMedia() throws SQLException {

		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT r.specializzazione, SEC_TO_TIME(AVG(TIME_TO_SEC(t.durata))) AS durata_media\n" + //
			"FROM Reparto r\n" + //
			"JOIN Medico m ON r.specializzazione = m.reparto\n" + //
			"JOIN Svolgimento s ON s.medico = m.id\n" + //
			"JOIN Trattamento t ON s.trattamento = t.codice\n" + //
			"WHERE t.tipo = 'operazione chirurgica'\n" + //
			"GROUP BY r.specializzazione;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Reparto: " + rs.getString("specializzazione") +
			"\nDurata media operazioni: " + rs.getString("durata_media") +
			"\n\n";
		}

		return result;
	}

	//Operazione 8: Stampa elenco dei trattamenti in attesa con la data concordata odierna

	public static String queryTrattamentiDaSvolgereOggi() throws SQLException {
		
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT t.codice, t.nome, t.data_concordata, t.ora_concordata, t.tipo, t.stato, t.paziente\n" + //
			"FROM Trattamento t\n" + //
			"WHERE t.data_concordata = CURDATE() AND t.stato = 'in attesa';"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Codice trattamento: " + rs.getString("codice") +
			"\nNome trattamento: " + rs.getString("nome") + 
			"\nData odierna: " + rs.getString("data_concordata") +
			"\nOra concordata: " + rs.getString("ora_concordata") +
			"\nTipo trattamento: " + rs.getString("tipo") +
			"\nStato attuale: " + rs.getString("stato") +
			"\n\n";
		}

		return result;
	}

	//Operazione 9: Stampa elenco dei pazienti che hanno effettuato più di un trattamento nello stesso giorno

	public static String queryPazientiConPiùTrattGiornalieri() throws SQLException {
		
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT p.codice_fiscale, p.nome, p.cognome, t.data_concordata, COUNT(t.codice) AS num_trattamenti_eseguiti\n" + //
			"FROM Paziente p\n" + //
			"JOIN Trattamento t ON p.codice_fiscale = t.paziente\n" + //
			"WHERE t.stato = 'eseguito'\n" + //
			"GROUP BY t.paziente, t.data_concordata\n" + //
			"HAVING COUNT(t.codice) > 1;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Codice fiscale paziente: " + rs.getString("codice_fiscale") +
			"\nNome: " + rs.getString("nome") +
			"\nCognome: " + rs.getString("cognome") +
			"\nData svolgimento trattamenti: " + rs.getString("data_concordata") +
			"\nTrattamenti sostenuti: " + rs.getString("num_trattamenti_eseguiti") +
			"\n\n";
		}

		return result;
	}

	//Operazione 10: Stampa del numero di utilizzi delle apparecchiature diagnostiche ordinate per data di acquisto

	public static String queryApparecchiaturePerDataAcquisto() throws SQLException {
		
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT ad.codice_alfa, ad.nome AS nome_apparecchiatura, ad.data_di_acquisto, \n" + //
			"       SUM(u.utilizzi) AS num_utilizzi\n" + //
			"FROM Apparecchiatura_diagnostica ad\n" + //
			"LEFT JOIN Utilizzo u ON ad.codice_alfa = u.apparecchiatura_diagnostica\n" + //
			"GROUP BY ad.codice_alfa, ad.nome, ad.data_di_acquisto\n" + //
			"ORDER BY ad.data_di_acquisto ASC;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Codice alfa apparecchiatura: " + rs.getString("codice_alfa") +
			"\nNome: " + rs.getString("nome_apparecchiatura") +
			"\nData d'acquisto: " + rs.getString("data_di_acquisto") +
			"\nNumero di utilizzi: " + rs.getString("num_utilizzi") +
			"\n\n";
		}

		return result;
	}

	//Operazione 11: Archiviazione dei trattamenti non eseguiti

	public static void updateTrattamenti() throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"UPDATE Trattamento\n" + //
			"SET stato = 'non eseguito'\n" + //
			"WHERE data_concordata <= CURDATE() AND stato = 'in attesa';"
		);

		ps.executeUpdate();
	}

	//Operazione 12: Stampa elenco degli infermieri per reparto ordinati per numero di interventi assistiti
	
	public static String queryInfermieriPerInterventi() throws SQLException {
		
		String result = "";

		PreparedStatement ps = con.prepareStatement(
			"SELECT i.reparto, i.nome, i.cognome, COUNT(a.trattamento) AS interventi_assistiti\n" + //
			"FROM Infermiere i\n" + //
			"JOIN Assistenza a ON i.id = a.infermiere\n" + //
			"GROUP BY i.reparto, i.nome, i.cognome\n" + //
			"ORDER BY reparto, interventi_assistiti DESC;"
		);

		ResultSet rs = ps.executeQuery();

		while(rs.next()) {
			result += "Reparto: " + rs.getString("reparto") +
			"\nNome: " + rs.getString("nome") +
			"\nCognome: " + rs.getString("cognome") +
			"\nInterventi assistiti: " + rs.getString("interventi_assistiti") +
			"\n\n";
		}

		return result;
	}

	//Operazione 13: Inserimento nuovo membro del personale

	public static void inserisciMedico(String nome, String cognome, LocalDate dataDiNascita, String reparto) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Medico (nome, cognome, data_di_nascita, data_assunzione, reparto)\n" + //
			"VALUES \n" + //
			"(?, ?, ?, current_date(), ?)"
		);
		
		ps.setString(1, nome);
		ps.setString(2, cognome);
		ps.setDate(3, Date.valueOf(dataDiNascita));
		ps.setString(4, reparto);

		ps.executeUpdate();
	}

	public static void inserisciInfermiere(String nome, String cognome, LocalDate dataDiNascita, String reparto) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Infermiere (nome, cognome, data_di_nascita, data_assunzione, reparto)\n" + //
			"VALUES \n" + //
			"(?, ?, ?, current_date(), ?)"
		);
		
		ps.setString(1, nome);
		ps.setString(2, cognome);
		ps.setDate(3, Date.valueOf(dataDiNascita));
		ps.setString(4, reparto);

		ps.executeUpdate();
	}

	public static void inserisciPAmministrativo(String nome, String cognome, LocalDate dataDiNascita, String ruolo) throws SQLException {

		PreparedStatement ps = con.prepareStatement(
			"INSERT INTO Personale_amministrativo (nome, cognome, data_di_nascita, data_assunzione, ruolo)\n" + //
			"VALUES \n" + //
			"(?, ?, ?, current_date(), ?)"
		);
		
		ps.setString(1, nome);
		ps.setString(2, cognome);
		ps.setDate(3, Date.valueOf(dataDiNascita));
		ps.setString(4, ruolo);

		ps.executeUpdate();
	}
}

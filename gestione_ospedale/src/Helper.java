import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class Helper {
    
    //Query di utilità per la gestione delle operazioni

    public static String trovaCfPaziente(String nomePaziente, String cognomePaziente, LocalDate data_di_nascita) throws SQLException {
        
        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT codice_fiscale " +
            "FROM Paziente " + 
            "WHERE nome = ? AND cognome = ? AND data_di_nascita = ?"
        );

        ps.setString(1, nomePaziente);
        ps.setString(2, cognomePaziente);
        ps.setDate(3, Date.valueOf(data_di_nascita));
        
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getString("codice_fiscale");
        else
            throw new SQLException("Paziente non presente nel database");
    }

    public static int trovaCodiceTrattamento(String cfPaziente, String nome, LocalDate data) throws SQLException {

        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT codice " +
            "FROM Trattamento " + 
            "WHERE nome = ? AND data_concordata = ? AND paziente = ?"
        );

        ps.setString(1, nome);
        ps.setDate(2, Date.valueOf(data));
        ps.setString(3, cfPaziente);
        
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getInt("codice");
        else
            throw new SQLException("Trattamento non presente nel database");
    }

    public static String trovaTipoTrattamento(int codiceTrattamento) throws SQLException {

        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT tipo " +
            "FROM Trattamento " + 
            "WHERE codice = ?"
        );

        ps.setInt(1, codiceTrattamento);
        
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getString("tipo");
        else
            throw new SQLException("Trattamento non presente nel database");
    }

    public static int trovaIdMedico(String nome, String cognome) throws SQLException {

        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT id " +
            "FROM Medico " + 
            "WHERE nome = ? AND cognome = ?"
        );

        ps.setString(1, nome);
        ps.setString(2, cognome);
        
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getInt("id");
        else
            throw new SQLException("Medico non presente nel database");
    }

    public static String trovaCodiceApparecchiatura(String nome) throws SQLException {

        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT codice_alfa " +
            "FROM Apparecchiatura_diagnostica " + 
            "WHERE nome = ? "
        );

        ps.setString(1, nome);
        
        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getString("codice_alfa");
        else
            throw new SQLException("Apparecchiatura non presente nel database");
    }

    public static int ultimoIdMedico() throws SQLException {

        PreparedStatement ps = DbManager.getCon().prepareStatement(
            "SELECT MAX(id) AS ultimo_id " +
            "FROM Medico"
        );

        ResultSet rs = ps.executeQuery();

        if (rs.next())
            return rs.getInt("ultimo_id");
        else
            throw new SQLException("Id medico non trovato");
    }
}

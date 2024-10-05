DROP SCHEMA ospedale;
CREATE SCHEMA ospedale;
USE ospedale;

CREATE TABLE Paziente (
	codice_fiscale			char(16) PRIMARY KEY,
    nome					varchar(20) NOT NULL,
    cognome					varchar(20) NOT NULL, 
    data_di_nascita			date NOT NULL,
    email					varchar(50) NOT NULL,
    trattamenti_sostenuti 	smallint DEFAULT 0,
    
							UNIQUE (nome, cognome, data_di_nascita),
                            -- Vincolo sul formato della stringa del cf e dell'email
    CONSTRAINT check_cf   	CHECK (codice_fiscale REGEXP '[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]'),
    CONSTRAINT check_email	CHECK (email LIKE '%_@_%._%')
);

CREATE TABLE Trattamento (
								-- Codice incrementale assegnato all'inserimento
	codice						int AUTO_INCREMENT PRIMARY KEY,
    nome						varchar(100) NOT NULL,
    data_concordata				date NOT NULL,
    ora_concordata				time NOT NULL,
    tipo						varchar(50) NOT NULL,
								-- Trattamenti appena prenotati sono di default in attesa
	stato						varchar(20) DEFAULT 'in attesa',
    paziente					char(16) NOT NULL,
    data_prenotazione			date NOT NULL,
    durata						time,
    
								UNIQUE (nome, data_concordata, paziente),
	CONSTRAINT chk_durata		CHECK ((tipo = 'esame medico' AND durata IS NULL) OR (tipo <> 'esame medico')),
	CONSTRAINT check_tipo		CHECK (tipo IN ('esame medico', 'operazione chirurgica')), 
    CONSTRAINT check_stato		CHECK (stato IN ('in attesa', 'eseguito', 'non eseguito')),
    CONSTRAINT fk_trattamento	FOREIGN KEY (paziente) REFERENCES Paziente (codice_fiscale)
								-- Update e Cancellazione paziente non consentite (gli interventi sono entità deboli
                                -- nella nostra prima progettazione)
								ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE Reparto (
	specializzazione		varchar(50) PRIMARY KEY,
    piano					smallint NOT NULL,
    capacità_sale			smallint NOT NULL
);

CREATE TABLE Infermiere (
	id							int AUTO_INCREMENT PRIMARY KEY,
    nome						varchar(20) NOT NULL,
    cognome						varchar(20) NOT NULL, 
    data_di_nascita				date NOT NULL,
    data_assunzione				date,
    reparto						varchar(50) NOT NULL,
	turni_settimanali			varchar(21),
    
								UNIQUE (nome, cognome, data_di_nascita, data_assunzione),
								-- Formato turni settimanali: "M-M-P-N-N-P-M"
	CONSTRAINT chk_turni_i 		CHECK (turni_settimanali REGEXP '^[MPN](-[MPN]){6}$'),
    CONSTRAINT fk_infermiere	FOREIGN KEY (reparto) REFERENCES Reparto (specializzazione)
								-- Cancellazione e update dei reparti non consentite
								ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE Medico (
	id							int AUTO_INCREMENT PRIMARY KEY,
    nome						varchar(20) NOT NULL,
    cognome						varchar(20) NOT NULL, 
    data_di_nascita				date NOT NULL,
    data_assunzione				date,
								-- Reparto identifica anche la specializzazione del medico
    reparto						varchar(50) NOT NULL,
    turni_settimanali			varchar(21),
    interventi_eseguiti			smallint DEFAULT 0,
    
								UNIQUE (nome, cognome, data_di_nascita, data_assunzione),
	CONSTRAINT chk_turni_m 		CHECK (turni_settimanali REGEXP '^[MPN](-[MPN]){6}$'),
    CONSTRAINT fk_medico		FOREIGN KEY (reparto) REFERENCES Reparto (specializzazione)
								ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE Personale_amministrativo (
	id						int AUTO_INCREMENT PRIMARY KEY,
    nome					varchar(20) NOT NULL,
    cognome					varchar(20) NOT NULL, 
    data_di_nascita			date NOT NULL,
    data_assunzione			date,
    ruolo					varchar(50) NOT NULL,
    
							UNIQUE (nome, cognome, data_di_nascita, data_assunzione)
);

-- Gli infermieri saranno identificati da un id >= 500, i medici >= 700 e il p.a. >= 800
ALTER TABLE Infermiere AUTO_INCREMENT = 500;
ALTER TABLE Medico AUTO_INCREMENT = 700;
ALTER TABLE Personale_amministrativo AUTO_INCREMENT = 800;

CREATE TABLE Apparecchiatura_diagnostica (
							-- Il codice identificativo dell'apparecchiatura è alfanumerico
	codice_alfa				varchar(20) PRIMARY KEY,
    nome					varchar(50) NOT NULL,
    data_di_acquisto		date
);

CREATE TABLE Svolgimento (
									-- JOIN TABLE
	medico							int,
    trattamento						int,
    
    PRIMARY KEY						(medico, trattamento),
    CONSTRAINT fk_med_operante		FOREIGN KEY (medico) REFERENCES Medico (id)
									ON UPDATE RESTRICT ON DELETE RESTRICT,
	CONSTRAINT fk_tratt_svolto		FOREIGN KEY (trattamento) REFERENCES Trattamento (codice)
									ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Assistenza (
									-- JOIN TABLE
	infermiere						int,
	trattamento						int,
    
    PRIMARY KEY						(infermiere, trattamento),
    CONSTRAINT fk_inf_assistente	FOREIGN KEY (infermiere) REFERENCES Infermiere (id)
									ON UPDATE RESTRICT ON DELETE RESTRICT,
	CONSTRAINT fk_tratt_assisttito	FOREIGN KEY (trattamento) REFERENCES Trattamento (codice)
									ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Utilizzo (
										-- JOIN TABLE
	apparecchiatura_diagnostica			varchar(20),
    trattamento							int,
    utilizzi							smallint DEFAULT 1,
    
    PRIMARY KEY							(apparecchiatura_diagnostica, trattamento),
    CONSTRAINT fk_app_utilizzata		FOREIGN KEY (apparecchiatura_diagnostica) REFERENCES Apparecchiatura_diagnostica (codice_alfa)
										ON UPDATE RESTRICT ON DELETE RESTRICT,
	CONSTRAINT fk_tratt_richiedente		FOREIGN KEY (trattamento) REFERENCES Trattamento (codice)
										ON UPDATE CASCADE ON DELETE CASCADE
);

DELIMITER //
//
-- Trigger di aggiornamento dati
-- Aggiorna lo stato del trattamento in 'eseguito', trattamenti_sostenuti e interventi_eseguiti ad ogni svolgimento di trattamento
CREATE TRIGGER after_insert_svolgimento
AFTER INSERT ON Svolgimento
FOR EACH ROW
BEGIN
    DECLARE trattamento_stato VARCHAR(20);
    DECLARE trattamento_tipo VARCHAR(50);
    DECLARE medico1_reparto VARCHAR(50);
    DECLARE medico2_reparto VARCHAR(50);
    
    -- Ottieni lo stato del trattamento
    SELECT stato INTO trattamento_stato
    FROM Trattamento
    WHERE codice = NEW.trattamento;
    
    -- Ottieni il tipo di trattamento svolto
    SELECT tipo INTO trattamento_tipo
    FROM Trattamento
    WHERE codice = NEW.trattamento;
    
    -- Primo inserimento di uno svolgimento di trattamento
    IF trattamento_stato = 'in attesa' 
		THEN
        -- Aggiorna lo stato del trattamento in 'eseguito'
        UPDATE Trattamento
        SET stato = 'eseguito'
        WHERE codice = NEW.trattamento;

        -- Aggiorna il numero di trattamenti sostenuti dal paziente
        UPDATE Paziente
        SET trattamenti_sostenuti = trattamenti_sostenuti + 1
        WHERE codice_fiscale = (
            SELECT paziente 
            FROM Trattamento 
            WHERE codice = NEW.trattamento
        );
    END IF;

    -- Se il primo medico è già stato inserito
    IF trattamento_stato = 'eseguito'
		THEN
        -- Ottieni il reparto del medico che sto inserendo (secondo medico)
		SELECT reparto INTO medico2_reparto
		FROM Medico
		WHERE id = NEW.medico;
        
         -- Ottieni il reparto del primo medico
		SELECT reparto INTO medico1_reparto
		FROM Medico
		WHERE id = (
			SELECT medico
			FROM Svolgimento
			WHERE trattamento = NEW.trattamento
			LIMIT 1);
        
        IF medico1_reparto <> medico2_reparto THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il secondo medico deve appartenere allo stesso reparto del primo medico';
		END IF;
	END IF;
			
    -- Incrementa il numero di interventi sostenuti dal medico se il trattamento è un'operazione chirurgica
    IF trattamento_tipo = 'operazione chirurgica'
		THEN
        -- Aggiorna il numero di interventi sostenuti dal medico
        UPDATE Medico
        SET interventi_eseguiti = interventi_eseguiti + 1
        WHERE id = NEW.medico;
    END IF;
END; //

-- In "Assistenza" sono ammesse solo operazioni chirurgiche
CREATE TRIGGER before_insert_assistenza
BEFORE INSERT ON Assistenza
FOR EACH ROW
BEGIN
    DECLARE tipo_trattamento VARCHAR(50);

    -- Ottieni il tipo di trattamento
    SELECT tipo INTO tipo_trattamento
    FROM Trattamento
    WHERE codice = NEW.trattamento;

    -- Verifica se il tipo di trattamento è diverso da 'operazione chirurgica'
    IF tipo_trattamento <> 'operazione chirurgica' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il trattamento deve essere del tipo ''operazione chirurgica''';
    END IF;
END //

-- Vincolo di schema: In ogni reparto non possono essere presenti più di 3 medici 
CREATE TRIGGER before_insert_medico
BEFORE INSERT ON Medico
FOR EACH ROW
BEGIN
    DECLARE num_medici INTEGER;
    
    -- Conta il numero di medici nel reparto del nuovo medico
    SELECT COUNT(*) INTO num_medici
    FROM Medico
    WHERE reparto = NEW.reparto;
    
    -- Se il numero di medici nel reparto è maggiore o uguale a 3, genera un errore
    IF num_medici >= 3 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Impossibile inserire il medico nel reparto. Il limite massimo di medici è stato raggiunto.';
    END IF;
END; //

-- Vincolo di schema: Un paziente con più di 2 trattamenti in attesa non può prenotarne uno nuovo
CREATE TRIGGER before_insert_trattamento
BEFORE INSERT ON Trattamento
FOR EACH ROW
BEGIN
    DECLARE num_trattamenti_attesa INT;

    -- Conta il numero di trattamenti in attesa per il paziente
    SELECT COUNT(*) INTO num_trattamenti_attesa
    FROM Trattamento
    WHERE paziente = NEW.paziente AND stato = 'in attesa';

    -- Se il paziente ha più di due trattamenti in attesa, genera un errore
    IF num_trattamenti_attesa > 2 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il paziente ha già più di due trattamenti in attesa e non può prenotarne uno nuovo.';
    END IF;
END; //
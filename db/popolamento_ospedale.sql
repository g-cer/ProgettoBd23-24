-- Inserimento di pazienti
INSERT INTO Paziente (codice_fiscale, nome, cognome, data_di_nascita, email)
VALUES 
('ABCDEF12G34H567I', 'Mario', 'Rossi', '1990-05-15', 'mario.rossi@example.com'),
('LMNOPQ34R56S789T', 'Laura', 'Bianchi', '1985-08-25', 'laura.bianchi@example.com'),
('UVWXYZ56A78B901C', 'Giuseppe', 'Verdi', '1976-11-10', 'giuseppe.verdi@example.com');

-- Inserimento di trattamenti
INSERT INTO Trattamento (nome, data_concordata, ora_concordata, paziente, data_prenotazione, tipo)
VALUES 
('Ecografia Addominale', '2024-02-15', '09:00:00', 'ABCDEF12G34H567I', '2024-02-14', 'esame medico'),
('Radiografia Toracica', '2024-02-14', '10:30:00', 'LMNOPQ34R56S789T', '2024-02-14', 'esame medico'),
('Chirurgia Appendicite', '2024-02-17', '14:00:00', 'UVWXYZ56A78B901C', '2024-02-14', 'operazione chirurgica'),

('Esame medico 2 Rossi', '2024-02-15', '09:00:00', 'ABCDEF12G34H567I', '2024-02-14', 'esame medico'),
('Chirurgia 2 Verdi', '2024-02-21', '14:00:00', 'UVWXYZ56A78B901C', '2024-02-19', 'operazione chirurgica'),
('Chirurgia 1 Bianchi', '2024-02-21', '10:30:00', 'LMNOPQ34R56S789T', '2024-02-17', 'esame medico');

-- Inserimento di reparti
INSERT INTO Reparto (specializzazione, piano, capacità_sale)
VALUES 
('Cardiologia', 2, 5),
('Chirurgia', 4, 8),
('Radiologia', 1, 3);

-- Inserimento di infermieri
INSERT INTO Infermiere (nome, cognome, data_di_nascita, data_assunzione, reparto, turni_settimanali)
VALUES 
('Anna', 'Verdi', '1992-03-20', '2023-01-10', 'Chirurgia', 'P-N-N-M-M-P-N'),
('Luca', 'Bianchi', '1990-07-12', '2022-05-05', 'Radiologia', 'M-M-P-N-N-P-M'),

('Infermiere', '1', '1990-07-12', '2022-05-05', 'Radiologia', 'M-M-P-N-N-P-M');

-- Inserimento di medici
INSERT INTO Medico (nome, cognome, data_di_nascita, data_assunzione, reparto, turni_settimanali)
VALUES 
('Giorgio', 'Russo', '1980-09-05', '2021-03-15', 'Cardiologia', 'P-N-N-M-M-P-N'),
('Elena', 'Ferrari', '1975-12-18', '2020-02-20', 'Chirurgia', 'M-M-P-N-N-P-M'),

('Medico', '1', '1975-12-18', '2020-02-20', 'Chirurgia', 'M-M-P-N-N-P-M'),
('Medico', '2', '1980-09-05', '2021-03-15', 'Cardiologia', 'P-N-N-M-M-P-N');

-- Inserimento di personale amministrativo
INSERT INTO Personale_amministrativo (nome, cognome, data_di_nascita, data_assunzione, ruolo)
VALUES 
('Marco', 'Gallo', '1988-06-30', '2022-01-20', 'Segreteria'),
('Simona', 'Conti', '1985-04-14', '2023-04-05', 'Contabilità');

-- Inserimento di apparecchiature diagnostiche
INSERT INTO Apparecchiatura_diagnostica (codice_alfa, nome, data_di_acquisto)
VALUES 
('ECG123', 'Elettrocardiografo', '2023-08-10'),
('US456', 'Ecografo', '2022-11-05');

-- Inserimento di svolgimenti (trigger after_insert_svolgimento)
INSERT INTO Svolgimento (medico, trattamento)
VALUES 
(700, 3),
(701, 1),
(701, 4);

-- update durata dopo lo svolgimento dell'operazione
UPDATE Trattamento
SET durata = '01:30:00'
WHERE tipo = 'operazione chirurgica' AND codice = 3;

-- Inserimento di assistenze
INSERT INTO Assistenza (infermiere, trattamento)
VALUES 
(500, 3),
(501, 3);

-- Inserimento di utilizzi di apparecchiature diagnostiche
INSERT INTO Utilizzo (apparecchiatura_diagnostica, trattamento)
VALUES 
('ECG123', 1),
('US456', 2);

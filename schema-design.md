## MySQL Database Design
### Table: Doctors
- id: INT, primary key, auto increment, NOT NULL
- clinic_id: INT, foreign key -> clinics(id) (one-to-one), NOT NULL
- appointment_id: INT, foreign key (one-to-many)
- availability: DATETIME
### Table: Clinics
- id: INT, primary key, auto increment, NOT NULL
- doctor_id: INT, foreign key -> doctors(id) (many-to-one)
- clinic_adress: String, NOT NULL
### Table: Patients
- id: INT, primary key, auto increment, NOT NULL
- patient_adress: String, NOT NULL
- appointment_id: INT, foreign key -> appointments(id) (many-to-one)
### Table: Appointments
- id: INT, primary key, auto increment, NOT NULL
- doctor_id: INT, foreign key -> doctors(id) (one-to-one)
- patient_id: INT, foreign key -> patients(id) (one-to-one)
- date: DATETIME, NOT NULL
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)
### Table: Administrators
- id: INT, primary key, auto increment, NOT NULL

## MongoDB Collection Design
### Collection: Prescriptions
'''json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
}

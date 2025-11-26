import { showBookingOverlay } from './loggedPatient.js';
import { deleteDoctor } from '../api/doctorServices.js';
import { getPatientDetails } from '../api/patientServices.js';
export function createDoctorCard(doctor) {
  const card = document.createElement('div');
  card.className = 'doctor-card';
  const role = localStorage.getItem('userRole');
  const doctorInfo = document.createElement('div');
  doctorInfo.className = 'doctor-info';
  const nameElem = document.createElement('h3');
  nameElem.textContent = doctor.name;
  const specialtyElem = document.createElement('p');
  specialtyElem.textContent = `Specialization: ${doctor.specialty}`;
  const emailElem = document.createElement('p');
  emailElem.textContent = `Email: ${doctor.email}`;
  const timesElem = document.createElement('p');
  timesElem.textContent = `Available Times: ${doctor.availableTimes.join(', ')}`;
  doctorInfo.appendChild(nameElem);
  doctorInfo.appendChild(specialtyElem);
  doctorInfo.appendChild(emailElem);
  doctorInfo.appendChild(timesElem);
  const actionsContainer = document.createElement('div');
  actionsContainer.className = 'card-actions';
  if (role === 'admin') {
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'delete-btn';
    deleteBtn.textContent = 'Delete';
    deleteBtn.addEventListener('click', () => {
      const token = localStorage.getItem('authToken');
      deleteDoctor(doctor.id, token)
        .then(() => {
          alert('Doctor deleted successfully.');
          card.remove();
        })
        .catch((error) => {
          alert('Error deleting doctor: ' + error.message);
        });
    });
    actionsContainer.appendChild(deleteBtn);
  } else if (role === 'patient') {
    const bookBtn = document.createElement('button');
    bookBtn.className = 'book-btn';
    bookBtn.textContent = 'Book Now';
    bookBtn.addEventListener('click', () => {
      alert('Please log in to book an appointment.');
    });
    actionsContainer.appendChild(bookBtn);
  } else if (role === 'loggedPatient') {
    const bookBtn = document.createElement('button');
    bookBtn.className = 'book-btn';
    bookBtn.textContent = 'Book Now'; 
    bookBtn.addEventListener('click', () => {
      const token = localStorage.getItem('authToken');  
      if (!token) {
        alert('Authentication token not found. Please log in again.');
        window.location.href = '/';
        return;
      }
      getPatientDetails(token)
        .then((patient) => {
          showBookingOverlay(doctor, patient);  
        })
        .catch((error) => {
          alert('Error fetching patient details: ' + error.message);
        }); 
    });
    actionsContainer.appendChild(bookBtn);
  }
  card.appendChild(doctorInfo);
  card.appendChild(actionsContainer);
  return card;
} 

/*
Import the overlay function for booking appointments from loggedPatient.js

  Import the deleteDoctor API function to remove doctors (admin role) from doctorServices.js

  Import function to fetch patient details (used during booking) from patientServices.js

  Function to create and return a DOM element for a single doctor card
    Create the main container for the doctor card
    Retrieve the current user role from localStorage
    Create a div to hold doctor information
    Create and set the doctor’s name
    Create and set the doctor's specialization
    Create and set the doctor's email
    Create and list available appointment times
    Append all info elements to the doctor info container
    Create a container for card action buttons
    === ADMIN ROLE ACTIONS ===
      Create a delete button
      Add click handler for delete button
     Get the admin token from localStorage
        Call API to delete the doctor
        Show result and remove card if successful
      Add delete button to actions container
   
    === PATIENT (NOT LOGGED-IN) ROLE ACTIONS ===
      Create a book now button
      Alert patient to log in before booking
      Add button to actions container
  
    === LOGGED-IN PATIENT ROLE ACTIONS === 
      Create a book now button
      Handle booking logic for logged-in patient   
        Redirect if token not available
        Fetch patient data with token
        Show booking overlay UI with doctor and patient info
      Add button to actions container
   
  Append doctor info and action buttons to the car
  Return the complete doctor card element
*/

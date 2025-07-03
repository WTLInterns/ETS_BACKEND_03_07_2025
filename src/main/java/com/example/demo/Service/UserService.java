// package com.example.demo.Service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.example.demo.DTO.ScheduleDateBookingDTO;
// import com.example.demo.DTO.SchedulingBookingDTO;
// import com.example.demo.DTO.UserDTO;
// import com.example.demo.DTO.UserLoginRequestDTO;
// import com.example.demo.DTO.UserLoginResponseDTO;
// import com.example.demo.DTO.VendorDTO;
// import com.example.demo.DTO.VendorDriverDTO;
// import com.example.demo.Model.SchedulingBooking;
// import com.example.demo.Model.User;
// import com.example.demo.Model.Vendor;
// import com.example.demo.Model.VendorDriver;
// import com.example.demo.Repository.ScheduleBookingRepository;
// import com.example.demo.Repository.UserRepository;

// @Service
// public class UserService {

//     @Autowired
//     private ScheduleBookingRepository scheduleBookingRepository;

//     @Autowired
//     private RestTemplate restTemplate;
    
//     @Autowired
//     private UserRepository userRepository;;

//     public User createUser(User user){
//         user.setRole("ETS_USER");
//         return this.userRepository.save(user);
//     }


//     // public List<User> getAllUser(){
//     //     return this.userRepository.findAll();
//     // }

//     public User getUserById(int userId) {
//         return this.userRepository.findById(userId)
//                 .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
//     }

//     public User updateUser(int id, User user) {
//         User existingUser = userRepository.findById(id)
//             .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    
//         existingUser.setFirstName(user.getFirstName());
//         existingUser.setLastName(user.getLastName());
//         existingUser.setGender(user.getGender());
//         existingUser.setEmail(user.getEmail());
//         existingUser.setPickupLocation(user.getPickupLocation());
//         existingUser.setDropLocation(user.getDropLocation());
//         existingUser.setShiftTime(user.getShiftTime());
//         existingUser.setMobileNo(user.getMobileNo());


    
//         return userRepository.save(existingUser);
//     }
    


//     public List<UserDTO> getAllUser() {
//         List<User> userList = this.userRepository.findAll();
//         List<UserDTO> userDTOs = new ArrayList<>();
        
//         for (User user : userList) {
//             UserDTO userDTO = new UserDTO();
//             userDTO.setId(user.getId());
//             userDTO.setFirstName(user.getFirstName());
//             userDTO.setLastName(user.getLastName());
//             userDTO.setGender(user.getGender());
//             userDTO.setEmail(user.getEmail());
//             userDTO.setMobileNo(user.getMobileNo());
//             userDTO.setPassword(user.getPassword());
//             userDTO.setPickupLocation(user.getPickupLocation());
//             userDTO.setDropLocation(user.getDropLocation());
//             userDTO.setShiftTime(user.getShiftTime());
//             userDTO.setStatus(user.getStatus());
//             userDTO.setProfile(user.getProfile());
    
//             userDTOs.add(userDTO);
//         }
    
//         return userDTOs;
//     }


//     public void deleteUser(int id){
//         this.userRepository.deleteById(id);
//     }


//     public int countOfAllUser(){
//         List<User> user = this.userRepository.findAll();
//         int length = user.size();
//         return length;
//         }


        
        
        
// public UserLoginResponseDTO login(UserLoginRequestDTO request) {
//         UserLoginResponseDTO response = new UserLoginResponseDTO();

//         Optional<User> userOpt = userRepository.findByMobileNo(request.getMobileNo());

//         if (userOpt.isPresent()) {
//             User user = userOpt.get();

//             if (request.getPassword().equals(user.getPassword())) {
//                 response.setStatus("success");
//                 response.setMessage("Login successful");
//                 response.setUserId(user.getId());
//                 response.setFirstName(user.getFirstName());
//                 response.setLastName(user.getLastName());
//                 response.setRole(user.getRole());
//             } else {
//                 response.setStatus("invalid_password");
//                 response.setMessage("Incorrect password");
//             }
//         } else {
//             response.setStatus("not_found");
//             response.setMessage("User not found");
//         }

//         return response;
//     }


        


    
    
    

// }

// package com.example.demo.Controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.demo.DTO.UserDTO;
// import com.example.demo.DTO.UserLoginRequestDTO;
// import com.example.demo.DTO.UserLoginResponseDTO;
// import com.example.demo.Model.User;
// import com.example.demo.Service.UserService;

// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;


// @RestController
// @RequestMapping("/users")
// public class UserController {
    
//     @Autowired
//     private UserService userService;

//     @PostMapping("/createUser")
//     public User createUser(@RequestBody User user) {
//        return this.userService.createUser(user);
//     }

//     @GetMapping("/getUserId/{id}")
//     public User getUserById(@PathVariable int id){
//         return this.userService.getUserById(id);
//     }

//     // @GetMapping("/getAllUser")
//     // public List<User> getAllUser(){
//     //     return this.userService.getAllUser();
//     // }


//      @GetMapping("/getAllUser")
//     public List<UserDTO> getAllUser(){
//         return this.userService.getAllUser();
//     }

//     @PutMapping("/updateUser/{id}")
//     public User updateUserById(@PathVariable int id, @RequestBody User user){
//         return this.userService.updateUser(id, user);
//     }
    

//     @DeleteMapping("/deleteById/{id}")
//     public void deleteUserById(@PathVariable int id){
//          this.userService.deleteUser(id);
//     }

//     @GetMapping("/empCount")
//     public int getAllCount(){
//         return this.userService.countOfAllUser();    }

//     @PostMapping("/etsLogin")
//     public ResponseEntity<UserLoginResponseDTO> login(@RequestBody UserLoginRequestDTO loginDTO) {
//         UserLoginResponseDTO response = userService.login(loginDTO);
//         return ResponseEntity.ok(response);
//     }
// }

package com.revature.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.revature.model.Chef;


/**
 * The AuthenticationService class provides authentication functionality
 * for Chef objects. It manages the login, logout, and registration
 * processes, as well as session management for chefs. This service 
 * utilizes a ChefService to perform operations related to chefs and 
 * maintains a session map to track active sessions.
 */

public class AuthenticationService {

    /**
     * The service used for managing Chef objects and their operations.
     */

    @SuppressWarnings("unused")
    private ChefService chefService;

    /** A map that keeps track of currently logged in users, indexed by session token. */
    public static Map<String, Chef> loggedInUsers = new HashMap<>();

    /**
     * Constructs an AuthenticationService with the specified ChefService and a newly created HashMap for the LoggedInUsers.
     *
     * @param chefService the ChefService to be used by this authentication service
     */
    public AuthenticationService(ChefService chefService) 
    {
        this.chefService = chefService;
    }

    /**
     * TODO: Authenticates a chef by verifying the provided credentials. If successful, a session token is generated and stored in the logged in users map.
     * 
     * @param chef the Chef object containing login credentials
     * @return a session token if the login is successful; null otherwise
     */
    public String login(Chef chef) 
    {
        if(chef==null || chef.getUsername()==null || chef.getPassword()==null)
            return null;
        
        else
        {
            List<Chef> dbchefs=chefService.searchChefs(null);

            for(Chef x:dbchefs)
            {
                if(x.getUsername().equals(chef.getUsername()) && x.getPassword().equals(chef.getPassword()))
                {
                    String token = UUID.randomUUID().toString();
                    loggedInUsers.put(token,x);
                    return token;
                }
            }
            return null;
        }

        
        
    }

    /**
     * TODO: Logs out a chef by removing their session token from the LoggedInUsers map.
     *
     * @param token the session token of the chef to be logged out
     */

    public void logout(String token) 
    {
        if(token!=null)
            loggedInUsers.remove(token);    
    }

    /**
	 * TODO: Registers a new chef by saving the chef's information using ChefService.
	 *
	 * @param chef the chef object containing registration details
	 * @return the registered chef object
	 */
    public Chef registerChef(Chef chef) 
    {
        chefService.saveChef(chef);
        return chef;
    }

    /**
     * TODO: Retrieves a Chef object from the session token.
     *
     * @param token the session token used to retrieve the chef
     * @return the Chef object associated with the session token; null if not found
     */
    public Chef getChefFromSessionToken(String token) 
    {
        if(token!=null)
            return loggedInUsers.get(token);
        else
            return null;
    }
}

// package com.revature.service;

// import java.util.List;
// import java.util.Map;
// import java.util.UUID;
// import java.util.concurrent.ConcurrentHashMap;

// import com.revature.model.Chef;

// /**
//  * AuthenticationService - handles simple authentication/session management
//  * for Chef objects. This implementation is intentionally simple (in-memory
//  * session store, plain-text password checks) and intended for learning/demo
//  * purposes. For production use you should:
//  *  - Hash & salt passwords (never store plain text)
//  *  - Use secure, signed tokens (JWT) or a dedicated session store (redis)
//  *  - Add rate limiting / locking for repeated failed logins
//  */
// public class AuthenticationService {

//     private final ChefService chefService;

//     /**
//      * Thread-safe map of session-token -> Chef
//      */
//     public static final Map<String, Chef> loggedInUsers = new ConcurrentHashMap<>();

//     public AuthenticationService(ChefService chefService) {
//         this.chefService = chefService;
//     }

//     /**
//      * Attempt to authenticate using username + password contained in the supplied Chef object.
//      * If successful a session token is generated and returned. If authentication fails, returns null.
//      *
//      * Note: This method expects the incoming Chef object to contain a username and password.
//      *
//      * @param chef the incoming Chef carrying credentials
//      * @return session token if authentication succeeded, null otherwise
//      */
//     public String login(Chef chef) {
//         if (chef == null || chef.getUsername() == null || chef.getPassword() == null) {
//             return null;
//         }

//         // get all chefs and match by username/password
//         // chefService.searchChefs(null) is implemented to return all chefs when term==null
//         List<Chef> allChefs = chefService.searchChefs(null);
//         if (allChefs == null || allChefs.isEmpty()) {
//             return null;
//         }

//         for (Chef dbChef : allChefs) {
//             if (dbChef == null) continue;
//             if (chef.getUsername().equals(dbChef.getUsername())
//                     && chef.getPassword().equals(dbChef.getPassword())) {
//                 String token = UUID.randomUUID().toString();
//                 loggedInUsers.put(token, dbChef);
//                 return token;
//             }
//         }

//         return null;
//     }

//     /**
//      * Logs out a chef by removing the session token from the session store.
//      *
//      * @param token the session token
//      * @return true if a session was removed, false if token was null or not present
//      */
//     public boolean logout(String token) {
//         if (token == null) return false;
//         return loggedInUsers.remove(token) != null;
//     }

//     /**
//      * Registers (saves) a new Chef using ChefService.
//      * ChefService.saveChef will create or update based on chef.id.
//      *
//      * @param chef the chef to register
//      * @return the saved Chef (with id populated if it was created)
//      */
//     public Chef registerChef(Chef chef) {
//         if (chef == null) return null;
//         chefService.saveChef(chef);
//         return chef;
//     }

//     /**
//      * Retrieve the Chef object associated with a session token.
//      *
//      * @param token the session token
//      * @return the Chef if present, otherwise null
//      */
//     public Chef getChefFromSessionToken(String token) {
//         if (token == null) return null;
//         return loggedInUsers.get(token);
//     }

//     /**
//      * Convenience method to check whether a token is valid.
//      *
//      * @param token session token
//      * @return true if token exists in the session store
//      */
//     public boolean isTokenValid(String token) {
//         if (token == null) return false;
//         return loggedInUsers.containsKey(token);
//     }
// }

1. Qbounce -> Saas platform 
2. email input -> {'data':['email1@gmial.com','email2@gmail.com','email3@gmial.com',.....'emailn@gmial.com']} + API KEY
3. Output -> 
{'results':[
{'email':'email1@gmial.com','accepted':true,'type':'type1','reason':'reason1'}, 
{'email':'email2@gmial.com','accepted':true,'type':'type2','reason':'reason2'}, 
{'email':'email3@gmial.com','accepted':true,'type':'type3','reason':'reason3'}, 
.....'emailn@gmial.com']}

type values -> PUBLIC, PRIVATE , TEMPORARY, ROLE_BASED,UNKNOWN
reason ex : Invaid email/role-based/ unknown/spam trap

4. Databse Tables: domain_cache , email_cache 
5. settings table for preferences of customer :
            tenant id -> is_public_accepted : true
                         is_private_accepted:true
                         is_temp_accepted:true
                         is_role_based_accepted:true
6. Implement prisma setup in java - migrations ( sql files to be executed in order and record of changes made)
                
                
7. Flow of validation ; 
            i. Emails + API KEY input 
            ii. Extract domain 
            iii. check domain_cache table 
            iv. if domain present in cache 
            if type == TEMPORARY -> return type as TEMPORARY
            if type == PUBLIC -> continue for email validation 
            if type == PRIVATE -> continue for email validation 
            if type == UNKNOWN -> continue for email validation 
            if domain not present in cache ->  
                1.  check cache for each email : 
                     if present return the value and save in db cache else call zerobounce api and save result in db cache and also update cache for  domain based on type of result.



gold , accept_all,alias_address -> return valid
mailbox_not_found,no_dns_entries,possible_typo,does_not_accept_mail,failed_syntax_check,global_suppression,toxic -> return invalid 
role_based -> return private
antispam_system,greylisted,timeout_exceeded -> return unknown


free_email  - true -> public 
do_not_email - disposable -> temporary

for unknown -> return error unable to respond at the moment 


///////





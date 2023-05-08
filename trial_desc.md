# Coding Task Description

## Assignment Instructions

1. In your own working environment, make a local clone of this repo.  (Do not make a GitHub fork.  That's public!)

```https://github.com/interview4aria/loan-svc-challenge.git```

2. Code the exercise.  A copy of this task description is in the file “trial_desc.md”.
If Scala is not your primary language, do the best you can within a limit of 3 hours. 

3. When you’re done, make a zip of your repo like this and email it back to us.

```git archive --format zip --output [your-zipped-filename-here] master```

4. Be prepared to present your code and run a demonstration during your follow-up interview.


## Task
 
Build a web service that, given a customer’s phone number and credit score, returns a set of 
available loan offers for that customer. The service should use HTTP and run on port 9000. 
 
### Inputs
* Phone number: a 13 digit number in the format CCCAAANNNNNNN, where CCC is 
the country code, AAA is an area code, and NNNNNNN is a 7 digit phone number. 
For example, 0013142298247. 
* Credit score: a floating point number in the range [0.0,1.0]. 
 
### Outputs
* An array of loan offers, where a loan offer consists of three attributes: 
  * Amount: a positive integer representing the loan principal, in USD 
  * Fee: an integer in the range [0,100] representing a percentage interest rate 
on the loan 
  * Term: a positive integer representing the duration of the loan, in days 

The set of available loan offers for a customer are uniquely determined by their credit score 
and a currently running experiment, as described in the following section. 
 
## Experiment format
 
An “experiment file” is a JSON definition of what offers to give what customers. Only one 
experiment may be active at any given time. An experiment is active for a particular time 
range, and specifies what offers to give customers with a particular score. The format of the 
experiment file is a list, called experiments, where each item in the list has the following 
properties: 
 
### Experiments
| Property   | Description |
|------------|-------------|
| name       | <string, name of Experiment>. Length must be <= 20 characters 
| startDate  | <string, in format 'YYYY­MM­DD'>
| endDate    | <string, in format 'YYYY­MM­DD'>
| offers     | <list of Offer> 


#### Offer
| Property   | Description |
|------------|-------------|
| minScore   | <double> Value in [0.0,1.0]. This is the minimum score a customer may have to receive this offer.
| amount     | <integer> Value in [10,50000]
| fee        | <integer> Value in [0,100]
| term       | <integer> Value in [1,365] 

 
Your service may simply always load the experiment file located in your class path.
An experiment file with more than one active experiment at a time, or with an invalid parameter in
any experiment, should be rejected with an error during loading (when the service is starting up).
Customers should receive all offers they are qualified for; e.g. if a customer has a minScore within the
threshold of multiple offers, they should receive all of those offers. If no experiment is currently active,
all customers should receive no offers. 
 
An example experiments file is: 

~~~~
{
    "experiments": [
        {
            "name": "Test high offers",
            "startDate": "2020-05-01",
            "endDate": "2020-05-31",
            "offers": [
                {
                    "minScore": 0.9,
                    "amount": 5000,
                    "fee": 10,
                    "term": 90
                },
                {
                "minScore": 0.5,
                "amount": 100,
                "fee": 10,
                "term": 90
                }
            ]
        },
        {
            "name": "Free for all",
            "startDate": "2020-06-01",
            "endDate": "2099-12-31",
            "offers": [
                {
                    "minScore": 0.9,
                    "amount": 1000,
                    "fee": 5,
                    "term": 90
                },
                {
                    "minScore": 0.5,
                    "amount": 100,
                    "fee": 10,
                    "term": 90
                },
                {
                    "minScore": 0.0,
                    "amount": 10,
                    "fee": 10,
                    "term": 30
                }
            ]
        }
    ]
}
~~~~

In this case, from May 1st to May 31st, 2020, the first experiment (“Test high offers”) is 
active. A customer with a score >= 0.9 should receive offers (amount,fee,term) of 
(5000,10,90) and (100,10,90). A customer with a score >= 0.5 should receive only one offer 
of (100,10,90). A customer with a score < 0.5 should receive no offers. From June 1st to 
December 31st, the second experiment (“Free for all”) is active, and the customer offers 
should change accordingly. 


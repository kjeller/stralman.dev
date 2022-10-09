---
title: "Protohackers - 1: Prime Time"
date: 2022-10-09
author: Karl Strålman
categories: ["dev_challenges"]
---

**Introduction**

This is the second entry of a series that cover solutions to the
[Protohackers](https://protohackers.com/) challenges. I choose to solve the first problem (Smoke test/TCP echo server) using x86_64 assembly. For my sanity I won't solve this one that way.. and you'll soon realise why (Hint: Because it requires JSON string parsing).

Onwards to the problem itself! This post will cover "Problem 1: Prime time". A full description of this problem can be found [here](https://protohackers.com/problem/1). The task is to write a simple JSON Web API backend that handles one very specific request. Checking if a number is prime or not. Example of a correctly formatted request: `{"method":"isPrime","number":123}` which should yield the response `{"method":"isPrime","prime":false}`. More details about what a correctly formatted request is and how to handle malformed requests in the next section. The same rule applies from the previous problem; The server should be able to handle multiple TCP clients simultaneously and can be written in whatever language you see fit. Now you might understand why I prefer not to do this in assembly. If you're still not 100% convinced please feel free to solve it that way yourself. Anyways, I choose to solve this problem using Rust since I've promised myself to do learn Rust for a while now. It also seemed like a good idea to me now [when even the Linux kernel has started to get a little rusty](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/commit/?id=8aebac82933ff1a7c8eede18cab11e1115e2062b).

In summary, this blog post will cover the following topics:
- TCP server in Rust handling multiple clients simultaneously
- JSON parsing in Rust (I "cheated" and used a lib)

Hopefully you will get a better understanding of these topics after reading this blog post or at least found some of the content interesting!

Resource links:
- Got my whole TCP server structure from here: https://doc.rust-lang.org/book/ch20-00-final-project-a-web-server.html

The full source code can be found on github - [here](https://github.com/kjeller/protohackers/tree/main/problem_1).

Note: My solution does not fully implement error handling and things as "gRaCEfuLLy eXiTinG tHreAds".

**JSON request/response protocol**

The problem description states:
*A conforming request object has the required field method, which must always contain the string "isPrime", and the required field number, which must contain a number. Any JSON number is a valid number, including floating-point values. [...] Extraneous fields are to be ignored.*

From the explanation above we can conclude that these are valid:
```json
{"method":"isPrime","number":123}
{"number":123, "method":"isPrime"} // ordering does not matter
{"method":"isPrime","number":123.123} // floats are valid numbers
{"method":"isPrime","number":123, "ignore": "me"} // extra fields are ignored
```

But prime numbers are **whole numbers** so how do we handle floats? This should have been stated in the problem description but is not mentioned anywhere. There are a number of different ways of handling the decimals in the float:
- Respond with `"prime":false` for all floats
- Round down/up and handle the resulting whole number
- Truncate and handle the resulting whole number

This line in the description is also important to consider: *Each request is a single line containing a JSON object, terminated by a newline character ('\n', or ASCII 10).* 

Then we also have to consider what a malformed request is and obviously a malformed request is a request that does not fulfill all the requirements to be a well-formed valid request:
```json
{"method":"isPrime"} // a required field is missing
{"method":"isPrime","number":"123"} // number is a string
{"method":"isPrime","number":123 // missing end braces
{"mEtHoD":"isPrime","nUmBer":123} // ..you get it
//... and so on
```

*Whenever you receive a malformed request, send back a single malformed response, and disconnect the client.* I chose to respond with `{}`  which is a malformed response since it does not contain the required response fields  `"method":"isPrime"` and `"prime":true/false`.

Three examples of sessions with request/response:
```json
// [x:y]
// x is the session number
// y is the message number

// [1:1] request from client
{"method":"isPrime","number":123}

// [1:2] response from server
{"method":"isPrime","prime":false}

// [2:1] request from client
{"method":"isPrime","number":2}

// [2:2] response from server
{"method":"isPrime","prime":true}

// [3:1] malformed request from client
{"method":"isPrime"}

// [3:2] response from server
{}
```

**Handle JSON request/response**

Now the JSON protocol is defined and eager to be parsed.
So for this I started writing a rudimentory JSON parser and quickly gave up... Then I found a JSON parsing crate for Rust that relieved me from the pain of implementing one myself ([this one](https://docs.rs/json/latest/json/)). For those non-rusty folks out there (including me) a crate is a library that can be imported either from disk or from a crate registry.

Here's the function that I wrote to parse a JSON request. I added some extra comments for clarity.
```rust
enum Method {
    IsPrime(i64),
}

// empty struct for error handling
struct ProtocolMalformed;

fn parse_json(json: &str) -> Result<Method, ProtocolMalformed> {
    let parse = json::parse(&json);
    
    // json::parse is very convenient where every field gets assigned
    // its own key in an object.
    if let Ok(data) = parse {
        // check if any of the required fields are missing
        if data["method"].is_null() || data["number"].is_null() {
            return Err(ProtocolMalformed);
        }

        // make sure that the method is set to a valid string, in this case "isPrime"
        if let Some(x) = data["method"].as_str() {
            match x {
                "isPrime" => {}, // allowed
                _ => return Err(ProtocolMalformed),
            }
        }
        
        // always handle JSON value for the number field as a float..
        if let Some(x) = data["number"].as_f64() {
            // ..and cast it to an integer.
            // in other words: the solution to json floats is to truncate!
            return Ok(Method::IsPrime(x as i64))
        } 
    }

    Err(ProtocolMalformed)
}
```

Now we can test the parse_json function without including any external lib because Rust includes it right out of its cargo tool (cargo test).
```rust
#[test]
    fn test_parse_json() {
        let requests = vec![
            "{\"method\":\"isPrime\",\"number\":123}".to_string(),
            "{\"method\":\"isPrime\",\"number\":1}".to_string(),
            "{\"method\":\"isPrime\",\"number\":7119040.0}".to_string(),
			
            // how should we handle this?
            "{\"method\":\"isPrime\",\"number\":7119040.123}".to_string(), 
            "{}".to_string(),
        ];

        let expected = vec![
            Ok(Method::IsPrime(123)),
            Ok(Method::IsPrime(1)),
            Ok(Method::IsPrime(7119040)),
            Ok(Method::IsPrime(7119040)), // oh right
            Err(ProtocolMalformed),
        ];

        for (i, r) in requests.iter().enumerate() {
            let method = parse_json(r);
            assert!(method == expected[i]);
        }
    }
```
That is all for parsing JSON requests, what is left to handle in the JSON protocol is to form a response.
The result from `fn parse_json(json: &str) -> Result<Method, ProtocolMalformed>`  is handled:

```rust
let method = parse_json(&request_line);

match method {
  // the return value from parse_json is Result<Method, ProtocolMalformed>
  // either it is Ok -> Method or Err -> ProtocalMalformed
	Ok(m) => {
		let response = handle_response(&m);
		// TODO send successful response
	},
	Err(_) => {
		// TODO send malformed response
	},
}
```

The response message is created in the function `fn handle_response(method: &Method) -> String` and the return value is the finalized JSON string. 
```rust
struct ResponseMessage {
    method_id: String, // method field value
    method_response: String, // response field value, for isPrime this is "prime"
    result: bool,
}

impl ResponseMessage {
    fn new() -> ResponseMessage {
        ResponseMessage {
            method_id: "".to_string(),
            method_response: "".to_string(),
            result: false,
        }
    }

    fn process(&mut self, method: &Method) {
        match method {
            Method::IsPrime(x) => {
                if *x < 0 {
                    self.result = false;
                } else {
                    // here we use to externa primes crate
                    self.result = primes::is_prime(*x as u64);
                }
                self.method_id = "isPrime".into();
                self.method_response = "prime".into();
            }
        }
    }
    
    // the actual json response is constructed here
    fn to_json_string(&self) -> String {
        return format!("{{\"method\":\"{}\",\"{}\":{}}}\n",
            self.method_id.as_str(), self.method_response.as_str(), self.result);
    }
}

fn handle_response(method: &Method) -> String {
    // create response message
    let mut result = Message::new();

    result.process(&method);
    
    // without ';' this value is returned from its scope
    //which is the return call of the function
    result.to_string()
}
```

**TCP Server in Rust**

What is left to do is the TCP server stuff which can be summarized into:
- Create and bind to a socket
- Listen for clients to connect, spawn a thread for each client that connects (very prone to DDOS attacks do not do this)
- Assign each thread a task
- Gracefully exit threads when done (I skip this)

```rust
const BIND_ADDR: &str = "0.0.0.0:48879";
const DEF_ERROR_RESP: &str = "{}";

fn main() {
    let listener = TcpListener::bind(BIND_ADDR).unwrap();

    for stream in listener.incoming() {
        let stream = stream.unwrap();
        
        println!("Connection established!");
        thread::spawn(|| {
            handle_connection(stream);
        });
    }
}
```

handle_connection reads and parses bytes from clients and responds accordingly to the JSON protocol. Above I intentionally left out some TODOs that is implemented here, the actual reading and writing from/to TCP clients.

```rust
fn handle_connection(mut stream: TcpStream) {
    let mut buf_reader = BufReader::new(stream.try_clone().unwrap());
    let mut buf_writer = BufWriter::new(&mut stream);

    loop {
        
        let mut request_line = String::new();
        let num_bytes = buf_reader.read_line(&mut request_line).unwrap();
        let method = parse_json(&request_line);
    
        println!("Read {} bytes: {} ", num_bytes, request_line);

        if num_bytes == 0 {
            break;
        }
        
        match method {
            Ok(m) => {
                let response = handle_response(&m);
                buf_writer.write_all({
                    println!("Valid json: Sending response {}", &response);
                    response.as_bytes()
                }).unwrap();
                buf_writer.flush().unwrap();
            },
            Err(_) => {
                println!("Malformed json '{}': responding with err", request_line);
                buf_writer.write_all({
                    DEF_ERROR_RESP.as_bytes()
                }).unwrap();
                buf_writer.flush().unwrap();
                break;
            },
        }
    }
    println!("Disconnect client!");
}
```

...and that is all for this post, thanks for reading!
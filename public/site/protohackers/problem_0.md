---
root: .components.layouts.MarkdownLayout
title: "Protohackers - 0: Smoke Test"
date: 2022-09-26
author: Karl Strålman
tags:
  - "dev_challenges"
---

## Introduction

This is the first entry of a series that will cover solutions to the
[Protohackers](https://protohackers.com/) challenges. Hopefully this will not be the first and only
entry to this series, but we'll see.

To be more specific about the contents of this post, I will cover my solution to "Problem 0: Smoke
Test". A full description of this problem can be found [here](https://protohackers.com/problem/0).
To spare you being redirected to some external site: The task is to write a TCP server that echoes
back payloads to its origin. Simple right? Protohackers does not enforce any specific programming
languages to be used for any of the problems. When my friend Martin suggested these problems to me I
immediately felt "Oh nice more stuff to add to my TODO pile" but then I thought ".. maybe this is an
excuse for me to delve into something that I haven't really touched since university classes -
assembly code". That is why I choose to write this TCP server in x86_64 assembly in Linux.

In summary, this blog post will cover the following topics:

- Lessen the pain of writing in "pure" x86 - intro to nasm
- Using system calls (syscalls) in x86?
- Accept multiple TCP clients simultaneously?
- Comparison of assembled/compiled "hand written" x86 vs C?

Hopefully you will get a better understanding of these topics after reading this blog post

Resource links:

- https://filippo.io/linux-syscall-table/
- https://github.com/hhampush/asmwebservice
- https://nullprogram.com/blog/2015/05/15/
- https://astharoshe.net/2020-08-31-Threads_the_Assembler_way.html
- https://aeryz.github.io/docs/small-projects/x86_64-tcp-server.html
    - This person solved multithreading in a slightly different more gracious way.

The full source code can be found on
github - [here](https://github.com/kjeller/protohackers/tree/main/problem_0).

**Lessen the pain of writing in "pure" x86**

Netwide Assembler (NASM) is one of the most popular x86 assembler out there and according
to https://www.nasm.us it is portable to nearly every modern platform. For me there were mainly two
reasons I choose to use nasm. 1) It was the first assembler I found when searching for x86
assemblers and 2) because of It having preprocessor (which I realised after searching for assemblers
online). This saved me the headache of using "pure" assembly programming - if there even is such a
thing today.

Definitions in NASM works in a similar way to C; so you can define functions like this:

```nasm
; from the nasm docs:
%define ctrl 0x1F &
%define param(a,b) ((a)+(a)*(b))
mov byte [param(2,ebx)], ctrl ’D’
; which will expand to
mov byte [(2)+(2)*(ebx)], 0x1F & ’D’
```

and of course the much simpler `%define <constant name> <constant value>`  which helped when keeping
track of the syscall constants:

```nasm
; ...
%define sys_read	0
%define sys_write	1
%define sys_socket	41
%define sys_accept	43
%define sys_bind	49
%define sys_listen	50
%define sys_fork	57
%define sys_exit	60
; ...
```

In hindsight I didn't use the preprocessor at its full, powerful and almighty extent but even as
little as having defines helped a lot.

**Using syscalls in x86**

A quick recap of what syscalls are. Syscalls or system calls are part of the Linux library that
allows user space applications request access to services in the kernel. In my case I for example
want access to a network device, my network card, to bind sockets and communicate with other devices
on the network. A thing to note is that all the syscalls functions  `read()`, `write()`.. and so on
are wrappers to `syscall()` which is pretty obvious when working with x86 in Linux. Documentation
for the wrappers of syscall can be found in section 2 of the manual e.g. `man read 2` in any Linux
shell returns:

```bash
NAME
       read - read from a file descriptor

SYNOPSIS
       #include <unistd.h>

       ssize_t read(int fd, void *buf, size_t count);

DESCRIPTION
				...
```

Using syscalls in assembler is really just "*C with extra steps*". Set the syscall "ID" in the %rax
register and prepare its arguments in %rsi, %rdi and %rdx and execute. Let's look at the simplest of
syscalls - `fork()` where only %rax is used:

```nasm
; fork()
mov rax, sys_fork ; remember %define sys_fork? Oh yes
syscall
```

The equivalent way of using the syscall in C to call fork:

```c
#include <sys/syscall.h>
#include <stdio.h>
#include <unistd.h>

int main() {
  pid_t ret = syscall(57); // 57 == fork()

  if (ret == 0) {
    printf("In child process\n");
  } else {
    printf("In parent process\n");
  }
}
```

.. which can also be achieved by just using the `fork()`  wrapper function like a sane person.
Wrapper functions are useful if you want to write more readable and probably less-crash prone code.

**Comparing C to assembly**

First the server needs create and bind to a socket to be able to listen to clients at all. This is
done using the `socket()` and `bind()` calls. But since there is not much logic involved there I
won't cover those parts in the code examples below.

To serve multiple clients simultaneously I have to either think about multithreading och
multiprocessing in the application.

**Accepting TCP clients**

The server polls for clients to connect with the `accept()` syscall. When a client connects a file
descriptor (fd) is returned and a client handling process is spawned with the `fork()` syscall. I
choose to do it this way since forking a process with `fork()` is the easier than creating a thread
with `clone()` and worrying about setting up its stack correctly. I also considered, and even spent
some time on, trying to get the pthread library to work. But I shortly gave up after somewhat
successfully linking pthread and getting strange runtime errors.

When calling `fork()` the return value can be used to differentiate the logic between the parent and
the child process. `fork()` return 0 in the child process and the child pid in the parent process.

<table>
<tr>
<th>C</th>
<th>x86_64</th>
</tr>
<tr>
<td>

```c
// srv_loop
while (1) {
	int cl_fd = accept(srv_fd, NULL, NULL);

	if (cl_fd < 0) {
	  continue;
	}

	pid_t pid = fork();

	// 0 is returned in the child process
	if (pid == 0) {
      // in child process
	} else if (pid > 0) {
	  // in parent process
	}
}
```

</td>
<td>

```nasm
; fork for every client that connects
srv_loop:
    ; server fd is stored in r8 from previous socket() and bind() calls
    
    ; accept(fd, addr(NULL), addrlen(NULL), flags)
    mov rax, sys_accept 
    mov rdi, r8
    mov rsi, 0
    mov rdx, 0
    syscall
    
    ; 0 is returned in the child process
    cmp rax, 0
    jl srv_loop
    mov r9, rax ; r9 fd to client

    ; fork()
    mov rax, sys_fork 
    syscall

    ; child process pid
    cmp rax, 0
    je echo

    ; in server process
    jmp srv_loop
	
echo:
    ; in child process
```

</td>
</tr>
</table>

The client handling process uses the blocking syscall `read()` to read from the fd that is mapped to
the client socket and writes the same payload back using the syscall `write()`.
<table>
<tr>
<th>C</th>
<th>x86_64</th>
</tr>
<tr>
<td>

```c
// cl_loop, tcp echo code
char buffer[MSGLEN];
int r;

while( (r = read(cl_fd, buffer, MSGLEN)) > 0 ) {
  write(cl_fd, buffer, r);
}
close(cl_fd);
```

</td>
<td>

```nasm
echo:
    ; (the echo label will be used, I promise)

cl_loop:
    ; read(fd, buf, count)
    mov rax, sys_read
    mov rdi, r9
    mov rsi, msgbuffer
    mov rdx, MSGLEN
    syscall

    ; continue write from buffer if read bytes > 0
    cmp rax, 0
    jbe cl_exit

    ; write(fd, buf, count)
    mov rdx, rax ; rax contains result from prev read()
    mov rax, sys_write
    mov rdi, r9
    mov rsi, msgbuffer
    syscall
    jmp cl_loop

cl_exit:
    ; close() client fd 
    mov rax, sys_close 
    mov rdi, r9
    syscall
```

</td>
</tr>
</table>

(Both the `read()` and `write()`  function declarations are close to identical. The only difference
is the %rax id)

By combining the forking part with the echoing part we get a TCP echo server. One thing I didn't
include in the code samples above was socket handling. What do I mean by that? Well we have to close
the server socket from the client socket and vice versa since those resources are no longer
connected to each respective process. That is also the reason I struggled to get this to work
correctly. Every time I submitted my solution I got a weird timeout that I couldn't explain.

The complete x86_64
code [(or on github)](https://github.com/kjeller/protohackers/blob/main/problem_0/tcp_echo.asm):

```nasm
; TCP Echo Service implementation of RFC 862
; A solution to https://protohackers.com/problem/0
;
; Resources:
;  - Useful lookup table from: https://filippo.io/linux-syscall-table/

; sys/syscall.h
%define sys_read	0
%define sys_write	1
%define sys_close	3
%define sys_socket	41
%define sys_accept	43
%define sys_bind	49
%define sys_listen	50
%define sys_fork	57
%define sys_exit	60

; TCP configurations
%define MSGLEN		4096
%define PORT		0xefbe ; 48879
%define sin_family	2 ; AF_INET
%define sin_type	1 ; SOCK_STREAM
%define sin_addr	0 ; INADDR_ANY

section .text
exit:
    mov rdi, 0
    mov rax, sys_exit
    syscall

global _start
_start:
    ; fd = socket(AF_INET, SOCK_STREAM, 0);
    mov rax, sys_socket
    mov rdi, sin_family
    mov rsi, sin_type
    mov rdx, 0 ; default protocol
    syscall
    mov r8, rax ;r8 contains server fd

    push dword sin_addr
    push word PORT
    push word sin_family

    ; bind(fd, *addr, addrlen)
    mov rax, sys_bind
    mov rdi, r8
    mov rsi, rsp
    mov rdx, 16 ; for sockaddr_in (IPv4)
    syscall
    cmp rax, 0
    jl exit

    ; listen(sockfd, queue len)
    mov rax, sys_listen 
    mov rdi, r8
    mov rsi, 10
    syscall

; fork for every client that connects
srv_loop:
    ; accept(fd, addr(NULL), addrlen(NULL), flags)
    mov rax, sys_accept 
    mov rdi, r8
    mov rsi, 0
    mov rdx, 0
    syscall
    cmp rax, 0
    jl srv_loop
    mov r9, rax ; r9 fd to client

    ; fork()
    mov rax, sys_fork 
    syscall

    ; child process pid
    cmp rax, 0
    je echo

    ; close() client fd from main process
    mov rax, sys_close 
    mov rdi, r9
    syscall
    jmp srv_loop

; if (pid == 0) {
;   close(srv_fd);
;   echo(cl_fd);
;   close(cl_fd);
; }
echo:
    ; close() server fd from child process
    mov rax, sys_close 
    mov rdi, r8
    syscall

cl_loop:
    ; read(fd, buf, count)
    mov rax, sys_read
    mov rdi, r9
    mov rsi, msgbuffer
    mov rdx, MSGLEN
    syscall

    ; continue write from buffer if read bytes > 0
    cmp rax, 0
    jbe cl_exit

    ; write(fd, buf, count)
    mov rdx, rax ; rax contains result from prev read()
    mov rax, sys_write
    mov rdi, r9
    mov rsi, msgbuffer
    syscall
    jmp cl_loop

cl_exit:
    ; close() client fd 
    mov rax, sys_close 
    mov rdi, r9
    syscall

segment .bss
    msgbuffer: resb MSGLEN

```

It might also be interesting to compare the executable binary size differences between the C
application compiled and linked by GCC and the assembler code assembled by NASM (and linked
with `ld`). The interested thing is that the assembled application is smaller than the compiled
application. This is even true even when compiling with `-O3` flag optimisation flags enabled.

```bash
ls -al bin/
-rwxrwxr-x 1 kjell kjell  5696 sep 19 19:20 tcp_echo_asm
-rwxrwxr-x 1 kjell kjell 16416 sep 19 19:20 tcp_echo_clang
-rwxrwxr-x 1 kjell kjell 16368 sep 19 19:20 tcp_echo_clang_O3
```

The next section is more of a small reference for those who wants to try to build and run x86 on
Linux themselves. If you have no interest in doing this you can skip it. That's all for this post,
thank you for your time I hope you found it interesting.

**Building the application**

Let's say we want to build an application from source file `code.asm`.
This line produces an object file: `nasm -f elf64 -g -o code.o code.asm`
Great now we have to link the object file to make it an executable. But what linker can we use?

- Using ld
  To use ld as a linker you have to include a `_start` label.

```nasm
global _start
_start:
    ; execution starts here
```

Link object file: `ld code.o -o exec_code`

- Using gcc
  To use GCC as a linker you have to include a `main` label.

```nasm
global main
main:
    ; execution starts here
```

Link object file: `gcc -no-pie code.o -o exec_code`
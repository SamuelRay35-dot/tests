# StringConcatenation Debugging Exercise

## Overview
The stringconcatenation service is implemented in both client and server, but has **4 bugs** that prevent it from working correctly according to the protocol specification.

The Correct Protocol is in the README.md

---

## The 4 Bugs

### Bug #1:  "type" was for concat not stringconcatenation
**Location:** `SockServer`, line 177

**The Problem:**
    The SockSrver only allows for string1 to be formed but the protocol requires both string1 and string2

**The Fix:**
    Added a second testField to allow for string2 to be formed

**Why it matters:** 
    If only one string is allowed then the function is useless as it would not concat anything

**How did you find this:**
    Reading the server code and comparing it to the protocol given

### Bug #2:  testField only tested string1 not both that and string2
**Location:** `SockServer`, line 184

**The Problem:**
    Checked for type concat instead of stringconcatenation

**The Fix:**
    Changed the type to the correct one

**Why it matters:** 
    If the type value is wrong then the correct fucntion won't run or will give "ok" false value

**How did you find this:**
    Reading server code and comparing it to protocol

### Bug #3:  json.put "str1"
**Location:** `SockClient`, line 74

**The Problem:**
    Checked for "str1" instead of "string1"

**The Fix:**
    Changed to string1 

**Why it matters:** 
    Once again if wrong variables are being checked then the function won't run as expected

**How did you find this:**
    Checking for bug that only printed the second value

### Bug #4:  json.put only the second string
**Location:** `SockClient`, line 75

**The Problem:**
    Was only res.put the second scanner line

**The Fix:**
    res.put after each scanner to get both string values

**Why it matters:** 
    This caused only the second string to be displayed

**How did you find this:**
    Checking for bug that only printed the second value

Repeat for other bugs

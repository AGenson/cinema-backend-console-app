# Cinema Backend - Console Application

Java application using Spring Boot. The application has the following features:
- In-memory storage (reset when closing)
- User management + security restrictions
- Movie & Film management
- Order of tickets

Environment used to develop:
- IntelliJ
- Java 1.8
- Linux

## Credentials

There are 2 users already in the in-memory database:
- Username: "**staff**" & Role: **"STAFF"**
- Username: "**customer**" & Role: **"CUSTOMER"**

Both passwords are: **"password"**

## Menu Map

You can find below a menu map to help you navigate.

#### Without Logging In

- See movie catalog
    - Movie selection:
        - Select seats & Order
            - **Identification**
            - **Order confirmation**
- Identify
    - Sign up
    - Log in
- Leave

#### When Logged In As Customer

- See movie catalog
    - Movie selection:
        - Select seats & Order
            - **Order confirmation**
- Identify
    - Sign up
    - Log in
- See Profile *(info & orders)*
- Leave

#### When Logged In As Staff

- Go to staff section
    - Manage movies
        - Add movie
        - Select movie
            - Edit title
            - Remove
    - Manage rooms
        - Add room
        - Select room
            - Edit movie
            - Remove
    - Manage users
        - Select user
            - Add / Remove staff privilege
- ... *(same options as customer)*

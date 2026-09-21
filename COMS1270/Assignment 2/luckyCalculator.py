# Madhava Nallamilli   9/20/2023
# Assignment 2
# This assignmnet is mean't to allow the student to have some practice using mathmatical operators and the random module


import random as rand
# NOTE: Function definitions should go here!
print("Lucky Calculator!")
print()
print("By: Madhava Nallamilli")
print("[COM S 127 G]")
print()
# Determine initial player choice
print("What would you like to do?")
print()
choice = input("[c]alculator, [l]ucky number, [q]uit: ")
print()

def calc(one, two, op) :

    if op == "+" :
        print("The result of the calculation was: "+str(one+two))
    elif op == "-" :
        print("The result of the calculation was: "+str(one-two))

    elif op == "*" :
        print("The result of the calculation was: "+str(one*two))

    elif op == "/" :
        if two == 0:
            print("The result of the calculation was: "+str(one/1))
        else:
            print("The result of the calculation was: "+str(one/two))
       

    elif op == "//" :
        if two == 0:
            print("The result of the calculation was: "+str(one//1))
        else:
            print("The result of the calculation was: "+str(one//two))
               

    elif op == "%" :

        if two == 0:
           print("The result of the calculation was: "+str(one%1))
        else:
            print("The result of the calculation was: "+str(one%two))
       

    elif op == "**" :
        print("The result of the calculation was: "+str(one**two))

    else :
        print("ERROR: You must enter either \"+\", \"-\", \"*\", \"/\", \"//\", \"%\", or \"**\"")


def luckyNumber(a, b):

        returnValVar = 0
        if a<b:
            returnValVar = rand.randrange(a, b+1)
        else:
            returnValVar = rand.randrange(b, a+1)
        return returnValVar

if choice == "c" :
    op = str(input("Please enter an Operator: "))
    one = int(input("Please Enter An Integer: "))
    two = int(input("Please Enter An Integer: "))
   
    calc(one, two, op)
                   
       


elif choice == "l" :
    a = int(input("Please Enter An Integer: "))
    b = int(input("Please Enter An Integer: "))
    output = str(luckyNumber(a, b))
    
    print("Your lucky number is: "+output)

elif choice == "q" :
    print("Maybe next time...")
else :
    print("ERROR: I did not understand your input... Please try again...")
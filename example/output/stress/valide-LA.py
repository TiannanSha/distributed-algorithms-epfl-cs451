import collections
from gettext import npgettext
from operator import truediv
from unittest import skip

def compareTwoFilesDecisions(filename1, filename2):
    file1 = open(filename1, 'r')
    file2 = open(filename2, 'r')
    lines1 = file1.readlines()
    lines2 = file2.readlines()
    if len(lines1) != len(lines2):
        print("Error! Different number of lines in two outputfiles")
        return
    
    for i in range(len(lines1)):
        decision1 = set(lines1[i][:-1].split(' '))
        decision2 = set(lines2[i][:-1].split(' '))
        if decision1.issubset(decision2) or decision2.issubset(decision1):
            skip
        else:
            print("Error in line"+str(i) + ", not subset")
            print("elements in" + filename1 + "but not " + filename2)
            print(sorted(decision1))
            print(sorted(decision2))
            for d1 in decision1:
                found = False
                for d2 in decision2: # check if d1 is in decision2
                    if d1==d2:
                        found = True
                        break
                if not found:
                    print(d1)
            print("elements in" + filename2 + "but not " + filename1)
            for d2 in decision2:
                found = False
                for d1 in decision1:
                    if d1==d2:
                        found = True
                if not found:
                    print(d2)
            
    file1.close()
    file2.close()

def checkIncludeMyProposals(output, config):
    outputfile = open(output, 'r')
    config = open(config, 'r')
    decisions = outputfile.readlines()
    myProposals = config.readlines()[1:]
    if (len(myProposals)!=len(decisions)):
        print("Error! num proposals diff from num decisions")
    for i in range(len(myProposals)):
        decision = set(decisions[i][:-1].split())
        proposal = set(myProposals[i][:-1].split())
        if not proposal.issubset(decision):
            print("Error: proposal not subset of decision line " + str(i))

numProc = 10
proc1 = './proc'
proc2 = './proc'
for i in range(1,numProc+1):
    print("checking proc" + str(i) + "with all other outputs")
    for j in range(1,numProc):
        if i==j:
            continue
        if i<10:
            proc1 = proc1+'0'+str(i)
        else:
            proc1 = proc1 + str(i)
        if j<10:
            proc2 = proc2+'0'+str(j)
        else:
            proc2 = proc2+str(j)

        output1 = proc1+'.output'
        output2 = proc2+'.output'
        compareTwoFilesDecisions(output1, output2)

        config1 = proc1+'.config'
        config2 = proc2+'.config'
        checkIncludeMyProposals(output1, config1)

        proc1 = './proc'
        proc2 = './proc'

print("OK")



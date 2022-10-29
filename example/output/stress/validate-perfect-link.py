import collections

file1 = open('./proc01.output', 'r')
lines = file1.readlines()
counts = collections.defaultdict(int)
for line in lines:
    # first two chars are d 
    splits = line[2:].split(' ') 
    sender = splits[0]
    msgId = splits[1]
    counts[sender] = counts[sender]+1

for sender in counts.keys():
    print(sender, counts[sender])

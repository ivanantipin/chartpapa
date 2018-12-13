out = open('out.txt', 'w')

lst = ['Membership','Company','ftserussell.com','Russell US Indexes','Russell 3000', 'All rights reserved.','respective licensors','Membership list','Russell US Indexes']

for r in open("list.txt"):
    tpl=r.split(' ')
    if len(tpl) < 6 and all(t not in r for t in lst):
        out.write(r)

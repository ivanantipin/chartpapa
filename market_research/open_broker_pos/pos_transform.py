cnt = 0

with open('positions.txt') as f_in, open('positions_out.txt', 'w') as f_out:
    for ln in f_in:
        cnt %= 3
        if cnt == 1:
            f_out.write(ln.strip().replace('ПАО','').replace('ОАО','').replace('"',''))
            f_out.write('=')
        if cnt == 2:
            f_out.write(ln)
        cnt += 1


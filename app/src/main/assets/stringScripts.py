#import fileinput

if __name__=='__main__':
    counter=1
    f1= open("EnglishInit.json",'r')
    f2= open('EnglishInit2.json','w')
    for line in f1:
        f2.write(line.replace('???','W??'))#str(counter)))
        if("???" in line):
            counter+=1

    print(counter)
    f1.close()
    f2.close()

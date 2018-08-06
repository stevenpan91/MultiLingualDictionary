import json

EnglishLetters=['A','a','B','b','C','c','D','d','E','e','F','f','G','g','H','h','I','i',\
                'J','j','K','k','L','l','M','m','N','n','O','o','P','p','Q','q','R','r',\
                'S','s','T','t','U','u','V','v','W','w','X','x','Y','y','Z','z']

def checkEachKey(jsonData,index,thisKey,recDepth,writeFile):
    #print(recDepth)
    #if(len(jsonData)>0):
    if(isinstance(jsonData,dict)):
        for index in jsonData:
            if(len(jsonData[index])>0):
                checkEachKey(jsonData[index],index,thisKey+index,recDepth+1,writeFile)

    if(recDepth>=3 and index=="Words"):
        f2.write("1\t"+str(getLetterIndex(jsonData[0],1))+"\t"+thisKey+"\t"+jsonData[0]+"\r")

def getLetterIndex(theWord,langIndex):
    firstLetter=theWord[0]
    if(langIndex==1):
        return getLetterIndexFromArray(firstLetter,EnglishLetters)

def getLetterIndexFromArray(firstLetter,LetterArray):
    count=0
    for letter in LetterArray:
        if (firstLetter==letter):
            return count//2 + 1

        count+=1

if __name__=='__main__':
    
    f1= open("EnglishInit.json",'r')
    f2= open('LookupIndex.txt','w')
    data=json.load(f1)

    checkEachKey(data,'','',0,f2)
    
    f1.close()
    f2.close()

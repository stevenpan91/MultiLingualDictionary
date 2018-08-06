import json

EnglishLetters=['A','a','B','b','C','c','D','d','E','e','F','f','G','g','H','h','I','i',\
                'J','j','K','k','L','l','M','m','N','n','O','o','P','p','Q','q','R','r',\
                'S','s','T','t','U','u','V','v','W','w','X','x','Y','y','Z','z']


RussianLetters=['ё','Ё','ъ','Ъ','я','Я','ш','Ш','е','Е','р','Р','т','Т','ы','Ы',\
                'у','У','и','И','о','О','п','П','ю','Ю','щ','Щ','э','Э','а','А',\
                'с','С','д','Д','ф','Ф','г','Г','ч','Ч','й','Й','к','К','л','Л',\
                'ь','Ь','ж','Ж','з','З','х','Х','ц','Ц','в','В','б','Б','н','Н','м','М']

MongolianLetters=['А','а','Б','б','В','в','Г','г','Д','д','Е','е','Ё','ё','Ж','ж',\
                  'З','з','И','и','Й','й','К','к','Л','л','М','м','Н','н','О','о',\
                  'Ө','ө','П','п','Р','р','С','с','Т','т','У','у','Ү','ү','Ф','ф',\
                  'Х','х','Ц','ц','Ч','ч','Ш','ш','Щ','щ','Ъ','ъ','Ы','ы','Ь','ь',\
                  'Э','э','Ю','ю','Я','я']

def checkEachKey(jsonData,index,thisKey,recDepth,writeFile,langIndex):
    #print(recDepth)
    #if(len(jsonData)>0):
    if(isinstance(jsonData,dict)):
        for index in jsonData:
            if(len(jsonData[index])>0):
                checkEachKey(jsonData[index],index,thisKey+index,recDepth+1,writeFile,langIndex)

    if(recDepth>=3 and index=="Words" and len(jsonData)>0 and len(jsonData[0])>0):
        f2.write(str(langIndex)+"\t"+str(getLetterIndex(jsonData[0],langIndex))+"\t"+thisKey+"\t"+jsonData[0]+"\r")

def getLetterIndex(theWord,langIndex):
    firstLetter=theWord[0]
    if(langIndex==1):
        return getLetterIndexFromArray(firstLetter,EnglishLetters)
    elif(langIndex==2):
        return getLetterIndexFromArray(firstLetter,MongolianLetters)
    elif(langIndex==3):
        return getLetterIndexFromArray(firstLetter,RussianLetters)

def getLetterIndexFromArray(firstLetter,LetterArray):
    count=0
    for letter in LetterArray:
        if (firstLetter==letter):
            return count//2 + 1

        count+=1

if __name__=='__main__':
    
    f1= open("EnglishInit.json",'r')
    f2= open('LookupIndex.txt','w')
    dataEng=json.load(f1)
    checkEachKey(dataEng,'','',0,f2,1)

    f3=open("MongolianInit.json",'r')
    dataMg=json.load(f3)
    checkEachKey(dataMg,'','',0,f2,2)

    f4=open("RussianInit.json",'r')
    dataRu=json.load(f4)
    checkEachKey(dataRu,'','',0,f2,3)
    
    f1.close()
    f2.close()
    f3.close()
    f4.close()

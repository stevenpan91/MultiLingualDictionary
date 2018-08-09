import json
import os
import glob

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

def checkEachKey(jsonData,index,thisKey,recDepth,langIndex,numIndex):
    #print(recDepth)
    #if(len(jsonData)>0):
    if(isinstance(jsonData,dict)):
        for index in jsonData:
            thisNumIndex=numIndex
            if(recDepth==0): #get this number
                thisNumIndex=index

            if(len(jsonData[index])>0):
                checkEachKey(jsonData[index],index,thisKey+index,recDepth+1,langIndex,thisNumIndex)

    if(recDepth>=3 and index=="Words" and len(jsonData)>0 and len(jsonData[0])>0):
        letterIndex=getLetterIndex(jsonData[0],langIndex)
        #writeFile=open('LookupIndex'+str(langValue)+"-"+str(letterIndex)+'.txt','a')
        aggregateIndex.write(str(langIndex)+"\t"+str(letterIndex)+"\t"+thisKey+"\t"+','.join(jsonData)+"\r")
        #writeFile.write(str(langIndex)+"\t"+str(letterIndex)+"\t"+thisKey+"\t"+''.join(jsonData)+"\r")
        #writeFile.close()

        #generate lookup indices based on key
        #key_index_num=int(numIndex)
        #key_index=str(key_index_num//25) #store by 50s x amount of langs
        #writeFile=open('LookupIndex'+"Key"+key_index+'.txt','a') #put all from same language in same file
        #writeFile.write(str(langIndex)+"\t"+str(letterIndex)+"\t"+thisKey+"\t"+''.join(jsonData)+"\r")
        #writeFile.close()

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

    for indexFile in glob.glob("LookupIndex*"):
        os.remove(indexFile)

    languages=3
    files=[]

    aggregateIndex=open('LookupIndex.txt','w')

    for count in range(0,languages):
        langValue=count+1

        whichJSON="Test"
        #I'm experimenting with two ways of making the JSON
        #"Init" and "Test"

        if(langValue==1):
            files.append(open("English"+whichJSON+".json",'r'))
        elif(langValue==2):
            files.append(open("Mongolian"+whichJSON+".json",'r'))
        elif(langValue==3):
            files.append(open("Russian"+whichJSON+".json",'r'))

        data=json.load(files[count])
        checkEachKey(data,'','',0,langValue,None)

    for f in files:
        f.close()

    aggregateIndex.close()

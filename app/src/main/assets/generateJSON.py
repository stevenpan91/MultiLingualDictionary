import fileinput
import json
if __name__=='__main__':
    count=1

    dataEng={}
    dataMg={}
    dataRu={}
    with open('MoToEng.txt') as f:
        for line in f:
            #print(line)
            #remove new line, add a tab to the front of part of speech then split
            components=line.replace('\n','').replace(". ",".\t").split('\t')
            if(components[2].find(',')!=-1):
                #components[2] and beyond are the eng definitions for mongolian words
                #',' indicates there's more than one definition
                #replace this comma with a tab then split.
                #comp_right consists of definitions in english for mongolian words
                comp_right=components[2].replace(',','\t').split('\t')
                components=[components[0],components[1]]
                for c in comp_right:
                    if c[0]==" ":
                        c=c[1:] #add definitions back after removing first space
                    components.append(c)


            #index
            dataEng[str(count)]={"W"+components[1].replace('.','').upper()+"00": {
                "Words": components[2:],
                "Pronunciation": "",
                "Definition": "",
                "Case/Tense": "",
                "Part Of Speech": components[1],
                "Example Sentences": ""
            }}

            dataMg[str(count)]={"W"+components[1].replace('.','').upper()+"00": {
                "Words": [components[0]],
                "Pronunciation": "",
                "Definition": "",
                "Case/Tense": "",
                "Part Of Speech": components[1],
                "Example Sentences": ""
            }}

            dataRu[str(count)]={"W"+components[1].replace('.','').upper()+"00": {
                "Words": "",
                "Pronunciation": "",
                "Definition": "",
                "Case/Tense": "",
                "Part Of Speech": "",
                "Example Sentences": ""
            }}

            count+=1

    with open('MongolianTest.json','w') as MGOut:
        json.dump(dataMg,MGOut,sort_keys=True,indent=4,ensure_ascii=False)

    with open('EnglishTest.json','w') as ENOut:
        json.dump(dataEng,ENOut,sort_keys=True,indent=4,ensure_ascii=False)

    with open('RussianTest.json','w') as RUOut:
        json.dump(dataRu,RUOut,sort_keys=True,indent=4,ensure_ascii=False)

    # with fileinput.FileInput('MoToEng.txt', inplace=True, backup='.bak') as file:
    #     for line in file:
    #
    #         if (line.find("n.") ==-1 and count<1409 and count>936):
    #             print(line.replace('\t', '\tn. '), end='')
    #         else:
    #             print(line,end='')
    #         count+=1


import fileinput
import json
if __name__=='__main__':
    count=1

    dataEng={}
    dataMg={}
    dataRu={}
    with open('MoToEng.txt') as f:
        for line in f:
            print(line)
            components=line.replace('\n','').replace(". ",".\t").split('\t')#remove newlines
            if(components[2].find(',')!=-1):
                comp_right=components[2].replace(',',',\t').split('\t')
                components=[components[0],components[1]]
                for c in comp_right:
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


import argparse
def map_generator(generated_name='g7.map',map_file='map.txt',size=10,s='b',e='t',b='p'):

    #read map
    with open(map_file) as f:
        content = f.readlines()
    
    #clean map
    mapText = []
    for line in content:
        line = line.replace(" ","")
        line = line[:size]
        mapText.append(line)
        
    #reshape map
    blocks = list()
    for row in range(size):
        for col in range(size):
            if mapText[row][col] == 'b':
                start = (row+1,col+1)
            elif mapText[row][col] == 't':
                end = (row+1,col+1)
            elif mapText[row][col] == 'p':
                blocks.append((row+1,col+1))

    #write map
    with open(generated_name,'w') as f:
        f.write(str(size)+'\n')
        f.write(str(start[0])+' '+str(start[1])+' '+str(end[0]) +' ' +str(end[1])+'\n')
        for row,col in blocks:
            f.write(str(row)+' '+str(col)+'\n')

def main():
    parser = argparse.ArgumentParser(description='Process the parameters to generate a map.')
    parser.add_argument('--size',help='the size of the map')
    parser.add_argument('--input',help='the name of input map')
    parser.add_argument('--output',help='the name of output map')
    args = parser.parse_args()
    map_generator(generated_name=args.output,map_file=args.input,size=int(args.size))
    # python map_generate.py --size 6 --input 'map.txt' --output '6x6.map'

main()

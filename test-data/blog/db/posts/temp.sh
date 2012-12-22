for (( i=1; i< 71; i++ ))
do
    id=$i
    if [[ $i -lt 10 ]]; then
        id="0$i"
    fi

    name="2012-02-01-title-$id.blogix"
    touch $name
    echo "----" > $name
    echo "title" >> $name
    echo "  title $id" >> $name
    echo "----" >> $name
    echo "date" >> $name
    echo "  2012-01-01 12:34" >> $name
    echo "----" >> $name
    echo "categories" >> $name
    echo "  main" >> $name
    echo "=====" >> $name
    echo "Sample description" >> $name

    
done

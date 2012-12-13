


cp -r bin/blog/* blog/.
cp bin/blog/blogix scripts/.

if [[ -d blog/export ]]; then
    rm -rf blog/export
fi

rm blog/blogix
rm blog/blogix.jar

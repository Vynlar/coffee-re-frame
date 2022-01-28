module.exports = {
  content: 
    {
      files: ['./src/**/*.{clj,cljs}'],
      extract: {
        cljs: content =>  {
          const match = content.match(/\[:.+\s{:class\s"(.+?)"/); 
          match && console.log(match);
          return match || [];
        },
        clj: content => {
          const match = content.match(/\[:.+{(?:.+)?:class\s+"(.+)"/);
          if (match && match[1]) {
            match[1] = match[1].split('.');
          }

          return match || [];
        }
      }
    },
  
  theme: {
    extend: {},
  },
  plugins: [],
}

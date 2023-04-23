function Field(props) {

  return (
    <>
      <div className="w-full flex gap-[2%] justify-center mb-[5vh]">
        <div className="h-[5vh] w-[70%] flex">
          <div className="
            h-[5vh] w-[40%] text-[3vh] text-black
          ">
            {props.text1}
          </div>
          {props.editable ?
            (props.drop ? 
              <select className="
                h-[5vh] w-[60%] bg-[#EAEAEA] rounded-[5px] px-[2%] text-[3vh] [cursor:pointer]
              ">
                {props.options.map((elem) =>
                  <option value={elem[0]} className="text-[3vh]">
                    {elem[1]}
                  </option>
                )}
              </select>
            :
              <input value={props.text2} onInput={(e)=>props.onValueChange(e.target.value)} className="
                h-[5vh] w-[60%] bg-[#EAEAEA] rounded-[5px] px-[2%]
                placeholder:text-[#999999] text-[3vh]
              "/>
            )
          :
            <div className="
              h-[5vh] w-[60%] bg-[#EAEAEA] rounded-[5px] px-[2%]
              text-black text-[3vh] leading-[5vh]
            ">
              {props.text2}
            </div>
          }
        </div>
      </div>
    </>
  )
}

export default Field;
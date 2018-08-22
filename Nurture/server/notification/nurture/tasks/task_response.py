class TaskResponse:

    @staticmethod
    def parse_line_from_response_file(line):
        try:
            # "ID","created_time","question_type","sub_question_type","status","answer_time","answer","option_ID","expired_time"
            line = line.strip()
            terms = line.split(',')
            assert len(terms) >= 9
            for i in range(9):
                term = terms[i]
                assert term[0] == '"'
                assert term[-1] == '"'
                terms[i] = terms[i][1:-1]
            print(terms)
            response = TaskResponse()
            response.id = int(terms[0])
            response.created_time = int(terms[1])
            response.question_type = int(terms[2])
            response.sub_question_type = int(terms[3])
            response.status = int(terms[4])
            response.answer_time = int(terms[5])
            response.answer = terms[6]
            response.option_id = int(terms[7])
            response.expired_time = int(terms[8])
            return response
        except:
            return None

    @staticmethod
    def parse_response_file(file_path):
        with open(file_path) as f:
            responses = [TaskResponse.parse_line_from_response_file(l) for l in f.readlines()[1:]]
        responses = [r for r in responses if r is not None]
        return responses

    def is_answered(self):
        return self.answer_time != 0

    def is_correct_answer(self):
        """
        return `True` or `False` if the correct answer can be determined. return `None` otherwise.
        """
        if not self.is_answered():
            return None

        if self.question_type == 6:  # ArithmeticTask
            return self.option_id == self.sub_question_type % 3 + 1
        elif self.question_type == 8:  # ImageTask
            return self.option_id == self.sub_question_type % 3 + 1

        return None

    def get_time_to_answer_sec(self):
        if not self.is_answered():
            return None
        return (response.answer_time - response.created_time) * 1e-3
